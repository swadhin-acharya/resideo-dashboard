#!/bin/bash
set -euo pipefail

# =============================================================================
# Resideo Sample Framework — Run Tests + Dashboard
# =============================================================================
# Usage:
#   ./run-tests.sh                    # Run tests only
#   ./run-tests.sh --with-dashboard   # Run tests + start dashboard
#   ./run-tests.sh --dashboard-only   # Start dashboard only (for viewing history)
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DASHBOARD_JAR="${SCRIPT_DIR}/../resideo-dashboard-standalone/target/resideo-dashboard-standalone-1.0.0-SNAPSHOT.jar"
WORKSPACE="${SCRIPT_DIR}"
GENERATED_ID=""

# Detect Java
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

generate_execution_id() {
    if command -v uuidgen &>/dev/null; then
        uuidgen
    elif command -v python3 &>/dev/null; then
        python3 -c "import uuid; print(uuid.uuid4())"
    else
        echo "resideo-$(date +%s)-$$"
    fi
}

run_tests() {
    echo "═════════════════════════════════════════════════════════════"
    echo "  Resideo Sample Automation Framework"
    echo "═════════════════════════════════════════════════════════════"
    echo ""

    GENERATED_ID=$(generate_execution_id)
    echo "▶ Execution ID: ${GENERATED_ID}"
    echo ""

    cd "${WORKSPACE}"

    mvn clean test \
        -Dexecution.id="${GENERATED_ID}" \
        -Dresideo.execution.id="${GENERATED_ID}" \
        -Dresideo.dashboard.url="${DASHBOARD_URL:-http://localhost:8080}" \
        2>&1

    echo ""
    echo "═════════════════════════════════════════════════════════════"
    echo "  Done — cucumber.json at: target/cucumber.json"
    echo "═════════════════════════════════════════════════════════════"
}

start_dashboard() {
    if [ ! -f "${DASHBOARD_JAR}" ]; then
        echo "Building dashboard JAR first..."
        cd "${SCRIPT_DIR}/.."
        mvn package -DskipTests -q
    fi

    echo "Starting Resideo Dashboard on http://localhost:8080 ..."
    cd "${SCRIPT_DIR}"
    $JAVA_CMD -jar "${DASHBOARD_JAR}" \
        --resideo.workspace="${WORKSPACE}" \
        --resideo.cucumber-json-path="target/cucumber.json" \
        --resideo.poll-interval-ms=3000 &
    DASHBOARD_PID=$!
    echo "Dashboard PID: ${DASHBOARD_PID}"
    echo "Open http://localhost:8080 in your browser"
    echo ""
}

# ── Main ────────────────────────────────────────────────────────────────────

case "${1:-}" in
    --with-dashboard)
        start_dashboard
        sleep 4
        run_tests
        echo ""
        echo "▶ Dashboard running at http://localhost:8080 (PID: ${DASHBOARD_PID})"
        echo "  To stop: kill ${DASHBOARD_PID}"
        ;;
    --dashboard-only)
        start_dashboard
        wait
        ;;
    *)
        run_tests
        ;;
esac
