#!/usr/bin/env bash
# Generate self-signed certificate and key for local HTTPS (e.g. https://localhost:8443)
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes \
  -subj "/CN=localhost/O=Local Dev/C=US"

echo "Created cert.pem and key.pem in $SCRIPT_DIR"
echo "Run the app with profile: selfsigned (HTTPS on port 8443)"
