#!/usr/bin/env bash
set -euo pipefail

# Starts user/product/order services and UI locally, waits for readiness, runs Postman tests via Newman,
# and cleans up background processes on exit.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

USER_DIR="$ROOT_DIR/api/services/user"
PRODUCT_DIR="$ROOT_DIR/api/services/product"
ORDER_DIR="$ROOT_DIR/api/services/order"
UI_DIR="$ROOT_DIR/ui"

USER_PORT=8081
PRODUCT_PORT=8082
ORDER_PORT=8083
UI_PORT=8084

# Exclude security for local runs so endpoints are open
SEC_EXCLUDE="org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration"

cleanup() {
  echo "\nCleaning up..." >&2
  for p in "$USER_PORT" "$PRODUCT_PORT" "$ORDER_PORT" "$UI_PORT"; do
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

echo "Killing any existing services on ports $USER_PORT,$PRODUCT_PORT,$ORDER_PORT,$UI_PORT..."
for p in "$USER_PORT" "$PRODUCT_PORT" "$ORDER_PORT" "$UI_PORT"; do
  lsof -ti tcp:"$p" | xargs -r kill -9 || true
done

echo "Starting user service (:$USER_PORT)..."
(
  cd "$USER_DIR"
  SERVER_PORT="$USER_PORT" mvn -q spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.autoconfigure.exclude=$SEC_EXCLUDE" &
) >/dev/null 2>&1 &

echo "Starting product service (:$PRODUCT_PORT)..."
(
  cd "$PRODUCT_DIR"
  SERVER_PORT="$PRODUCT_PORT" mvn -q spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.autoconfigure.exclude=$SEC_EXCLUDE" &
) >/dev/null 2>&1 &

echo "Starting order service (:$ORDER_PORT)..."
(
  cd "$ORDER_DIR"
  SERVER_PORT="$ORDER_PORT" USER_SERVICE_URL="http://localhost:$USER_PORT/api" PRODUCT_SERVICE_URL="http://localhost:$PRODUCT_PORT/api" \
  mvn -q spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.autoconfigure.exclude=$SEC_EXCLUDE" &
) >/dev/null 2>&1 &

wait_for_port "$USER_PORT" "user"
wait_for_port "$PRODUCT_PORT" "product"
wait_for_port "$ORDER_PORT" "order"

echo "Starting UI service (:$UI_PORT)..."
(
  cd "$UI_DIR"
  VITE_USER_API_URL="http://localhost:$USER_PORT/api" VITE_PRODUCT_API_URL="http://localhost:$PRODUCT_PORT/api" VITE_ORDER_API_URL="http://localhost:$ORDER_PORT/api" \
  npm run dev -- --port $UI_PORT &
) >/dev/null 2>&1 &

wait_for_port "$UI_PORT" "ui"

# Optional: basic health checks
for ep in \
  "http://localhost:$USER_PORT/actuator/health" \
  "http://localhost:$PRODUCT_PORT/actuator/health" \
  "http://localhost:$ORDER_PORT/actuator/health"; do
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
  --env-var userBaseUrl="http://localhost:$USER_PORT/api" \
  --env-var productBaseUrl="http://localhost:$PRODUCT_PORT/api" \
  --env-var orderBaseUrl="http://localhost:$ORDER_PORT/api" \
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


