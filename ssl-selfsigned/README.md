# Self-signed SSL (HTTPS for local / testing)

Use this to run the backend over HTTPS with a self-signed certificate (e.g. `https://localhost:8443`).

## 1. Generate the certificate

**Linux / Mac / Git Bash (Windows):**

```bash
cd ssl-selfsigned
./generate-self-signed.sh
```

**Windows (PowerShell):** run the commands from `generate-self-signed.ps1` or use Git Bash and the script above.

This creates:
- `cert.pem` – certificate
- `key.pem` – private key (do not commit)

## 2. Run the app with HTTPS

**Run from the project root** so the relative path `./ssl-selfsigned/` is correct.

Activate the `selfsigned` profile and run:

```bash
# Maven
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=mysql,selfsigned

# Or set env and run
$env:SPRING_PROFILES_ACTIVE="mysql,selfsigned"
.\mvnw.cmd spring-boot:run
```

The server will listen on **https://localhost:8443**. HTTP is disabled when this profile is active.

## 3. Trust the certificate (optional)

Browsers and clients will show a warning because the cert is self-signed. To avoid that:

- **Chrome/Edge:** open `https://localhost:8443`, click “Advanced” → “Proceed to localhost”.
- **curl:** use `curl -k` to skip verification (insecure; only for testing).
- **Java/custom client:** import `cert.pem` into your truststore, or disable SSL verification for localhost in dev only.

## 4. API base URL

Use `https://localhost:8443` in your frontend or API client (e.g. `app.api.base-url` or CORS) when testing with this profile.
