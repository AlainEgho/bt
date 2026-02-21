# Generate self-signed certificate and key for local HTTPS (e.g. https://localhost:8443)
# Requires OpenSSL (e.g. from Git for Windows, or install separately)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$openssl = Get-Command openssl -ErrorAction SilentlyContinue
if (-not $openssl) {
    Write-Error "OpenSSL not found. Install Git for Windows or OpenSSL and add it to PATH."
    exit 1
}

& openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes `
  -subj "/CN=localhost/O=Local Dev/C=US"

Write-Host "Created cert.pem and key.pem in $ScriptDir"
Write-Host "Run the app with profile: selfsigned (HTTPS on port 8443)"
