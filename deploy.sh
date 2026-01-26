#!/usr/bin/env bash
set -euo pipefail

PEM="${PEM:-$HOME/.ssh/toeic-master-key.pem}"
HOST="${HOST:-ubuntu@13.125.5.67}"
REMOTE_DIR="${REMOTE_DIR:-/home/ubuntu/app}"
SERVICE="${SERVICE:-screenmate}"

echo "==> Build jar"
./gradlew clean bootJar

JAR_PATH="$(ls -1 build/libs/*SNAPSHOT.jar | head -n 1)"
echo "==> Jar: $JAR_PATH"

echo "==> Ensure remote dir"
ssh -i "$PEM" "$HOST" "mkdir -p '$REMOTE_DIR'"

echo "==> Upload jar"
rsync -avz -e "ssh -i $PEM" "$JAR_PATH" "$HOST:$REMOTE_DIR/app.jar"

echo "==> Restart service"
ssh -i "$PEM" "$HOST" "sudo systemctl restart $SERVICE && sudo systemctl --no-pager --full status $SERVICE | sed -n '1,25p'"

echo "✅ Deploy done"
