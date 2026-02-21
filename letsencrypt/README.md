# Let's Encrypt – Elastic Beanstalk single instance

This folder holds PEM files for HTTPS when the app runs on **one** Elastic Beanstalk instance (no load balancer).

## 1. Paths on the instance

App root is `/var/app/current/`. Certs must be at:

| File        | Path on instance |
|------------|-------------------|
| Certificate | `/var/app/current/letsencrypt/fullchain.pem` |
| Private key | `/var/app/current/letsencrypt/privkey.pem` |

## 2. Environment variables (Beanstalk)

In **Elastic Beanstalk** → **Configuration** → **Software** → **Environment properties**, add:

| Name | Value |
|------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod,prod-mysql,ssl` |
| `SSL_CERTIFICATE_PATH` | `/var/app/current/letsencrypt/fullchain.pem` |
| `SSL_PRIVATE_KEY_PATH` | `/var/app/current/letsencrypt/privkey.pem` |
| `HTTPS_PORT` | `8443` |

Save and let the environment update.

## 3. Get certificates on the instance (first time)

**Prerequisites:** Your Beanstalk environment has a public IP or domain pointing to this single instance (no ALB). Port 80 must be free for the HTTP-01 challenge.

**3.1 SSH into the instance**

- EB Console: **Environment** → **Instance** → **Instance ID** (opens EC2) → **Connect** → **EC2 Instance Connect** (or Session Manager), or  
- CLI: `eb ssh`

**3.2 Install certbot and get a certificate**

Replace `yourdomain.com` with your domain (it must already point to this instance’s public IP).

```bash
# Install certbot (Amazon Linux 2)
sudo amazon-linux-extras install epel -y
sudo yum install -y certbot

# Stop the app so port 80 is free (single instance)
# Replace 'web' with your platform's app service if different (e.g. the name of your app)
sudo systemctl stop web

# Get certificate (standalone uses port 80)
sudo certbot certonly --standalone -d yourdomain.com --non-interactive --agree-tos -m your@email.com

# Copy into app folder and set owner (Beanstalk app user)
sudo mkdir -p /var/app/current/letsencrypt
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem /var/app/current/letsencrypt/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem /var/app/current/letsencrypt/
sudo chown -R webapp:webapp /var/app/current/letsencrypt

# Start the app again
sudo systemctl start web
```

**3.3 Open HTTPS port on the instance**

- **EC2** → **Security groups** → group attached to the Beanstalk instance → **Edit inbound rules** → add **TCP 8443** from `0.0.0.0/0` (or your frontend only). Save.

Your API will be reachable at `https://yourdomain.com:8443` (or use a reverse proxy on the same instance to serve 443 and proxy to 8443).

## 4. After a redeploy (certs are replaced)

On Beanstalk, a new deployment replaces `/var/app/current/`, so the certs in `letsencrypt/` are lost. For a **single instance** you can do either of the following.

**Option A – Copy from S3 on every deploy (recommended)**

1. After the first certbot run, upload certs once to S3 (encrypted bucket, restricted access):
   ```bash
   aws s3 cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem s3://YOUR-BUCKET/letsencrypt/fullchain.pem
   aws s3 cp /etc/letsencrypt/live/yourdomain.com/privkey.pem s3://YOUR-BUCKET/letsencrypt/privkey.pem
   ```
2. Add a **postdeploy** platform hook so every deploy copies them back.

   Create in your project:

   **`.platform/hooks/postdeploy/copy_letsencrypt.sh`**

   ```bash
   #!/bin/bash
   set -e
   BUCKET="YOUR-BUCKET"
   PREFIX="letsencrypt"
   APP_DIR="/var/app/current"
   mkdir -p "$APP_DIR/letsencrypt"
   aws s3 cp "s3://${BUCKET}/${PREFIX}/fullchain.pem" "$APP_DIR/letsencrypt/fullchain.pem"
   aws s3 cp "s3://${BUCKET}/${PREFIX}/privkey.pem" "$APP_DIR/letsencrypt/privkey.pem"
   chown webapp:webapp "$APP_DIR/letsencrypt"/*.pem
   ```

   Then:
   ```bash
   chmod +x .platform/hooks/postdeploy/copy_letsencrypt.sh
   ```
   Commit and deploy. Ensure the Beanstalk instance role has `s3:GetObject` on `s3://YOUR-BUCKET/letsencrypt/*`.

**Option B – Re-run certbot after each deploy**

After every deploy, SSH in and run the same certbot + copy + chown steps as in section 3.2 (and restart the app if needed). No S3 or hooks.

## 5. Renewal (single instance)

Let's Encrypt certs expire after 90 days. On the **same instance**:

```bash
sudo systemctl stop web
sudo certbot renew
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem /var/app/current/letsencrypt/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem /var/app/current/letsencrypt/
sudo chown webapp:webapp /var/app/current/letsencrypt/*.pem
sudo systemctl start web
```

If you use Option A (S3), upload the renewed certs to S3 after `certbot renew` so future deploys get the new certs.

You can add a cron on the instance to run `certbot renew` (and copy + restart) before expiry.

## 6. Security

- Do **not** commit `*.pem` (they are in `.gitignore`).
- Restrict S3 bucket and IAM so only the Beanstalk instance role can read the certs.
- Keep the security group for 8443 (or 443) as tight as possible (e.g. only your frontend or CDN).
