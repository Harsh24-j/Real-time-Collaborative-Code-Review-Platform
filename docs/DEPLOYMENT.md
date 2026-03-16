# Deployment Guide

Complete guide to deploy the Code Review Platform to AWS EC2.

## Prerequisites

- AWS account
- Domain name (optional but recommended)
- OpenAI API key
- Basic Linux knowledge

## Quick Deployment (30 minutes)

For detailed steps, see sections below.

```bash
# 1. Launch EC2 instance (t2.medium, Ubuntu 22.04)
# 2. SSH into server
ssh -i key.pem ubuntu@YOUR_IP

# 3. Clone repository
git clone https://github.com/Harsh24-j/Real-time-Collaborative-Code-Review-Platform.git /opt/code-review-platform
cd /opt/code-review-platform

# 4. Run setup script
sudo ./deployment/scripts/ec2-setup.sh

# 5. Configure environment
cp backend/.env.example backend/.env
nano backend/.env  # Add real credentials

# 6. Deploy application
./deployment/scripts/deploy.sh

# 7. Setup SSL (if you have a domain)
./deployment/scripts/ssl-setup.sh your-domain.com
```

Access: https://your-domain.com

## Detailed Deployment Steps

### AWS EC2 Strategy
- **Platform**: Amazon Linux 2 or Ubuntu 22.04 LTS.
- **Instance Type**: `t2.medium` (recommended for Docker + JVM).
- **Security Group**: Open ports 22 (SSH), 80 (HTTP), 443 (HTTPS), 8080 (API - optional).

### Docker Orchestration
- Use the included [docker-compose.yml](../docker-compose.yml) for one-command deployment.
- High-availability PostgreSQL and Redis configurations.

### Nginx Reverse Proxy
- Secure SSL termination using Let's Encrypt / Certbot.
- Path-based routing: `/` for frontend, `/api` and `/ws` for backend.
