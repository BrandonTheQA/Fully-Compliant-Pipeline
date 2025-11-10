#!/usr/bin/env bash
set -euo pipefail

# Starts monolith service and UI locally, waits for readiness, runs Postman tests via Newman,
# and cleans up background processes on exit.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

MONOLITH_DIR="$ROOT_DIR/api/services/monolith"
UI_DIR="$ROOT_DIR/ui"

MONOLITH_PORT=8080
UI_PORT=8084

# Exclude security for local runs so endpoints are open
SEC_EXCLUDE="org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration"

cleanup() {
  echo "\nCleaning up..." >&2
  for p in "$MONOLITH_PORT" "$UI_PORT"; do
    if lsof -ti tcp:"$p" >/dev/null 2>&1; then
      kill -9 $(lsof -ti tcp:"$p") || true
    fi
  done
}
trap cleanup EXIT

wait_for_port() {
  local port="$1"; local name="$2"; local tries=40
  for i in $(seq 1 "$tries"); do
    if nc -z localhost "$port" >/dev/null 2>&1; then
      echo "$name up on :$port"
      return 0
    fi
    sleep 1
  done
  echo "Timed out waiting for $name on :$port" >&2
  return 1
}

echo "Killing any existing services on ports $MONOLITH_PORT,$UI_PORT..."
for p in "$MONOLITH_PORT" "$UI_PORT"; do
  lsof -ti tcp:"$p" | xargs -r kill -9 || true
done

echo "Starting monolith service (:$MONOLITH_PORT)..."
(
  cd "$MONOLITH_DIR"
  SPRING_PROFILES_ACTIVE=local SERVER_PORT="$MONOLITH_PORT" mvn -q spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.autoconfigure.exclude=$SEC_EXCLUDE" &
) >/dev/null 2>&1 &

wait_for_port "$MONOLITH_PORT" "monolith"

echo "Starting UI service (:$UI_PORT)..."
(
  cd "$UI_DIR"
  npm run dev -- --port $UI_PORT &
) >/dev/null 2>&1 &

wait_for_port "$UI_PORT" "ui"

# Optional: basic health checks
for ep in \
  "http://localhost:$MONOLITH_PORT/actuator/health"; do
  curl -sf "$ep" >/dev/null || {
    echo "Health check failed: $ep" >&2
    exit 1
  }
done

echo "Ensuring newman is installed..."
if ! command -v newman >/dev/null 2>&1; then
  if command -v npm >/dev/null 2>&1; then
    npm -g --silent install newman >/dev/null 2>&1
  else
    echo "npm not found; please install Node.js/npm to use Newman." >&2
    exit 1
  fi
fi

RAND=$(date +%s)
COLLECTION="$ROOT_DIR/postman/IntegrationTest.postman_collection.json"

echo "Running Postman tests with Newman..."
newman run "$COLLECTION" \
  --env-var apiBaseUrl="http://localhost:$MONOLITH_PORT/api" \
  --env-var userName="John Doe" \
  --env-var userEmail="john.doe+${RAND}@example.com" \
  --env-var userPassword="SecurePassword123" \
  --env-var product1Name="Laptop" \
  --env-var product1Description="High-performance laptop" \
  --env-var product1Price=999.99 \
  --env-var product1Quantity=10 \
  --env-var product1Category="Electronics" \
  --env-var product1OrderQuantity=1 \
  --env-var product2Name="Mouse" \
  --env-var product2Description="Wireless mouse" \
  --env-var product2Price=29.99 \
  --env-var product2Quantity=50 \
  --env-var product2Category="Electronics" \
  --env-var product2OrderQuantity=2 \
  --env-var product3Name="Keyboard" \
  --env-var product3Description="Mechanical keyboard" \
  --env-var product3Price=79.99 \
  --env-var product3Quantity=25 \
  --env-var product3Category="Electronics" \
  --env-var product3OrderQuantity=1 \
  --color on

echo "All tests passed."


