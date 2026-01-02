#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

ECOMPOC_DIR="$ROOT_DIR/api/services/ecompoc"
UI_DIR="$ROOT_DIR/ui"

ECOMPOC_PORT=8080
UI_PORT=8084

SEC_EXCLUDE="org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration"

# Kill existing
echo "Killing existing services..."
for p in "$ECOMPOC_PORT" "$UI_PORT"; do
  lsof -ti tcp:"$p" | xargs -r kill -9 || true
done

echo "Starting ecompoc service (:$ECOMPOC_PORT)..."
(
  cd "$ECOMPOC_DIR"
  nohup mvn -q spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.autoconfigure.exclude=$SEC_EXCLUDE" \
    -Dspring.profiles.active=local \
    -Dserver.port="$ECOMPOC_PORT" > "$ROOT_DIR/ecompoc.log" 2>&1 &
)

echo "Starting UI service (:$UI_PORT)..."
(
  cd "$UI_DIR"
  nohup npm run dev -- --port $UI_PORT > "$ROOT_DIR/ui.log" 2>&1 &
)

wait_for_port() {
  local port="$1"; local name="$2"; local tries=120
  echo "Waiting for $name on :$port..."
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

wait_for_port "$ECOMPOC_PORT" "ecompoc"
wait_for_port "$UI_PORT" "ui"

echo "Services started."







