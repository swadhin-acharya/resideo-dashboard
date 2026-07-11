#!/bin/bash
# =============================================================================
# Test: Dashboard Parser Integration
# =============================================================================
# This script:
#   1. Starts the Resideo Dashboard in the background
#   2. Runs a sample test against it using curl
#   3. Verifies the dashboard correctly ingests cucumber.json
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DASHBOARD_JAR="${SCRIPT_DIR}/../resideo-dashboard-standalone/target/resideo-dashboard-standalone-1.0.0-SNAPSHOT.jar"
WORKSPACE="${SCRIPT_DIR}/target"
SAMPLE_CUCUMBER="${SCRIPT_DIR}/src/test/resources/sample-cucumber-results.json"
PASS=0
FAIL=0

# Detect Java — check Homebrew first, then fallback to default java
if [ -z "$JAVA_HOME" ]; then
    for candidate in /opt/homebrew/Cellar/openjdk/*/libexec/openjdk.jdk/Contents/Home; do
        if [ -f "$candidate/bin/java" ]; then
            export JAVA_HOME="$candidate"
            break
        fi
    done
fi
JAVA_CMD="${JAVA_HOME}/bin/java"
if [ ! -f "$JAVA_CMD" ]; then
    JAVA_CMD=$(command -v java 2>/dev/null || echo "")
fi
if [ -z "$JAVA_CMD" ]; then
    echo "❌ Java not found. Install via: brew install openjdk"
    exit 1
fi

check() {
    local test_name="$1"
    local expected="$2"
    local actual="$3"
    if echo "$actual" | grep -q "$expected"; then
        echo "  ✅ $test_name"
        PASS=$((PASS + 1))
    else
        echo "  ❌ $test_name (expected: $expected)"
        FAIL=$((FAIL + 1))
    fi
}

echo "═════════════════════════════════════════════════════════════"
echo "  Resideo Dashboard — Parser Integration Test"
echo "═════════════════════════════════════════════════════════════"
echo ""

# ── Step 1: Create workspace with cucumber.json ────────────────
mkdir -p "${WORKSPACE}"
cp "${SAMPLE_CUCUMBER}" "${WORKSPACE}/cucumber.json"
echo "✓ Sample cucumber.json copied to ${WORKSPACE}/cucumber.json"
echo ""

# ── Step 2: Start Dashboard ────────────────────────────────────
if [ ! -f "${DASHBOARD_JAR}" ]; then
    echo "Building dashboard JAR..."
    cd "${SCRIPT_DIR}/.."
    mvn package -DskipTests -q
    cd "${SCRIPT_DIR}"
fi

echo "Starting dashboard..."
$JAVA_CMD -jar "${DASHBOARD_JAR}" \
    --resideo.workspace="${SCRIPT_DIR}" \
    --resideo.cucumber-json-path="target/cucumber.json" \
    --resideo.poll-interval-ms=2000 \
    --server.port=9191 &
DASHBOARD_PID=$!
sleep 6  # wait for startup

# ── Step 3: Create an execution via API ─────────────────────────
echo ""
echo "── Creating execution ──────────────────────────────────────"
EXEC_RESPONSE=$(curl -s -X POST http://localhost:9191/api/v1/executions \
    -H "Content-Type: application/json" \
    -d '{
        "buildNumber": "1.0.0",
        "triggeredBy": "integration-test",
        "branch": "main",
        "platform": "ANDROID",
        "environment": "QA",
        "firmwareVersion": "1.3605.1550",
        "appVersion": "3.0.0",
        "executionType": "REGRESSION"
    }')
EXEC_ID=$(echo "$EXEC_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo "Execution ID: ${EXEC_ID}"
check "Execution created" "RUNNING" "$EXEC_RESPONSE"

# ── Step 4: Wait for watcher to pick up cucumber.json ──────────
echo ""
echo "── Waiting for workspace watcher to parse cucumber.json ────"
sleep 8  # poll interval is 2s, give it a few cycles

# ── Step 5: Verify the execution was updated with results ───────
echo ""
echo "── Verifying results ───────────────────────────────────────"
DETAIL=$(curl -s "http://localhost:9191/api/v1/executions/${EXEC_ID}")
check "Execution has pass count > 0" '"passCount"' "$DETAIL"

# ── Step 6: Check summary endpoint ─────────────────────────────
SUMMARY=$(curl -s "http://localhost:9191/api/v1/executions/summary")
check "Summary total executions > 0" '"totalExecutions"' "$SUMMARY"

# ── Step 7: Check analytics APIs ────────────────────────────────
TRENDS=$(curl -s "http://localhost:9191/api/v1/analytics/trends?days=7")
check "Analytics trends work" '"date"' "$TRENDS"

FLAKY=$(curl -s "http://localhost:9191/api/v1/analytics/flaky-tests")
check "Flaky test detection" '"scenarioName"' "$FLAKY"

# ── Cleanup ─────────────────────────────────────────────────────
echo ""
kill ${DASHBOARD_PID} 2>/dev/null
rm -f "${WORKSPACE}/cucumber.json"

echo ""
echo "═════════════════════════════════════════════════════════════"
echo "  Results: ${PASS} passed  |  ${FAIL} failed"
echo "═════════════════════════════════════════════════════════════"
