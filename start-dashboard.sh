#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== OpenQA Dashboard Local Edition ==="
echo ""

# Detect Java: prefer JAVA_HOME, then Homebrew, then system java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA="$JAVA_HOME/bin/java"
elif [ -x /opt/homebrew/opt/openjdk/bin/java ]; then
  JAVA=/opt/homebrew/opt/openjdk/bin/java
  export JAVA_HOME=/opt/homebrew/opt/openjdk
elif [ -x /usr/local/opt/openjdk/bin/java ]; then
  JAVA=/usr/local/opt/openjdk/bin/java
  export JAVA_HOME=/usr/local/opt/openjdk
elif command -v java &>/dev/null; then
  JAVA=java
else
  echo "ERROR: Java not found. Install a JDK 21+ via Homebrew:"
  echo "  brew install openjdk"
  exit 1
fi

# Ensure data directory exists
mkdir -p data

JAR_PATH="openqa-dashboard-standalone/target/openqa-dashboard-standalone-1.0.0-SNAPSHOT.jar"

if [ -f "$JAR_PATH" ]; then
    echo "Starting OpenQA Dashboard from JAR..."
    exec "$JAVA" -jar "$JAR_PATH"
fi

echo "JAR not found - building from source..."
mvn clean install -DskipTests -q

echo ""
echo "Starting OpenQA Dashboard..."
exec "$JAVA" -jar "$JAR_PATH"
