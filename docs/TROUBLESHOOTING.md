# Troubleshooting Guide

Common issues and solutions.

## Backend Issues

### Issue: Application won't start

**Symptoms:**
```
Error creating bean with name 'dataSource'
```

**Solution:**
1. Check database connection in `.env`
2. Verify PostgreSQL is running: `docker-compose ps`
3. Check logs: `docker-compose logs backend`

### Issue: JWT tokens not working

**Symptoms:**
```
401 Unauthorized on all protected endpoints
```

**Solution:**
1. Verify JWT_SECRET is set in `.env`
2. Check token expiration time
3. Ensure token is in Authorization header: `Bearer {token}`

## Frontend Issues

### Issue: Cannot connect to WebSocket

**Symptoms:**
- Live updates not appearing.
- Connection error in browser console.

**Solution:**
1. Check if `sockjs-client` and `@stomp/stompjs` are correctly configured.
2. Verify the WebSocket URL in `.env.production`.
3. Ensure Nginx is configured to upgrade connections to `websocket`.

## Deployment Issues

### Issue: Docker container fails to build

**Solution:**
1. Ensure Docker Desktop or Docker Engine is running.
2. Check for port conflicts (e.g., something already on 8080 or 5432).
3. Run `docker-compose build --no-cache` to force a fresh build.
