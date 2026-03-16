# Architecture Overview

## System Architecture Diagram

```
┌─────────────────────┐
│     Client          │
│    (Browser)        │
│  React 18 + Vite    │
└──────────┬──────────┘
           │
           │ HTTPS/WSS
           ▼
┌─────────────────────┐
│      Nginx          │
│  (Reverse Proxy)    │
│   Port: 80/443      │
└──────────┬──────────┘
           │
           ├──────────────────┬──────────────────┐
           │                  │                  │
           ▼                  ▼                  ▼
┌──────────────┐      ┌──────────────┐  ┌──────────────┐
│  Frontend    │      │   Backend    │  │  WebSocket   │
│   (React)    │      │ Spring Boot  │  │    Server    │
│  Port: 3000  │      │  Port: 8080  │  │ (STOMP/SockJS│
└──────────────┘      └──────┬───────┘  └──────────────┘
                             │
                   ┌─────────┼─────────┬────────────┐
                   │         │         │            │
                   ▼         ▼         ▼            ▼
            ┌──────────┐ ┌────────┐ ┌────────┐ ┌────────┐
            │PostgreSQL│ │ Redis  │ │OpenAI  │ │  SMTP  │
            │   DB     │ │ Cache  │ │  API   │ │ Email  │
            │Port: 5432│ │Port:6379│ │GPT-4   │ │Service │
            └──────────┘ └────────┘ └────────┘ └────────┘
```

## Component Details

### Frontend (React 18)

**Technologies:**
- React 18 with hooks
- Vite (build tool)
- Tailwind CSS (styling)
- Monaco Editor (code editing)
- Axios (HTTP client)
- SockJS + STOMP.js (WebSocket)
- Zustand (state management)

**Key Components:**
- `LoginForm.jsx`: User authentication
- `Dashboard.jsx`: User dashboard with stats
- `ReviewList.jsx`: List of code reviews
- `ReviewDetail.jsx`: Review detail view
- `CodeEditor.jsx`: Monaco-based code editor
- `CommentPanel.jsx`: Comment management
- `Leaderboard.jsx`: Gamification leaderboard

### Backend (Spring Boot 3.2)

**Technologies:**
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- Spring WebSocket
- PostgreSQL
- Redis
- OpenAI Java SDK

**Layers:**

1. **Controller Layer**
   - `AuthController`: Authentication endpoints
   - `ReviewController`: Review CRUD operations
   - `CommentController`: Comment management
   - `UserController`: User operations
   - `WebSocketController`: Real-time messaging

2. **Service Layer**
   - `UserService`: User business logic
   - `ReviewService`: Review processing
   - `CommentService`: Comment handling
   - `AIService`: OpenAI integration
   - `GamificationService`: Points and badges

3. **Repository Layer**
   - JPA repositories for all entities
   - Custom queries with @Query
   - Pagination support

4. **Security Layer**
   - JWT token generation/validation
   - BCrypt password hashing
   - Method-level security with @PreAuthorize

### Database (PostgreSQL 15)

**Schema:**

```sql
users (id, username, email, password, full_name, points, created_at)
code_reviews (id, title, description, code_content, language, status, creator_id, quality_score, created_at)
comments (id, review_id, user_id, comment_text, line_number, is_resolved, created_at)
ai_suggestions (id, review_id, suggestion_text, category, severity, created_at)
badges (id, name, description, points_required)
user_badges (user_id, badge_id, earned_at)
```

**Indexes:**
- `users.username` (unique)
- `users.email` (unique)
- `code_reviews.creator_id`
- `code_reviews.status`
- `comments.review_id`
- `comments.user_id`

### Cache (Redis 7)

**Usage:**
- Session storage
- WebSocket connection tracking
- Rate limiting
- Leaderboard caching

**Key Patterns:**
- `user:session:{userId}` - User sessions
- `review:cache:{reviewId}` - Review data
- `leaderboard:global` - Sorted set of users by points

### AI Integration (OpenAI GPT-4)

**Process:**
1. User submits code for review
2. Backend sends code to OpenAI API
3. GPT-4 analyzes code and returns suggestions
4. Suggestions categorized by type and severity
5. Stored in database and sent to frontend
6. Real-time updates via WebSocket

**Categories:**
- Best Practices
- Performance
- Security
- Code Style
- Potential Bugs

## Data Flow

### Authentication Flow

```
1. User enters credentials
2. Frontend → POST /api/auth/login
3. Backend validates credentials (BCrypt)
4. Backend generates JWT token
5. Token sent to frontend
6. Frontend stores in localStorage
7. All subsequent requests include token in header
```

### Code Review Creation Flow

```
1. User writes/pastes code
2. Frontend → POST /api/reviews
3. Backend creates review in PostgreSQL
4. Backend calls OpenAI API for analysis
5. AI suggestions stored in database
6. WebSocket broadcast to subscribers
7. Frontend receives update and displays review
```

### Real-time Comment Flow

```
1. User adds comment on specific line
2. Frontend → WebSocket /app/review/{id}/comment
3. Backend processes and saves to PostgreSQL
4. Backend broadcasts to /topic/review/{id}
5. All connected clients receive update
6. Comments appear instantly for all users
```

### Gamification Flow

```
1. User performs action (review, comment, approve)
2. Backend awards points based on action type
3. Check if new badge earned (points threshold)
4. Update user record in PostgreSQL
5. Update Redis leaderboard cache
6. Broadcast achievement via WebSocket
7. Frontend displays badge notification
```

## Security Architecture

### Authentication
- JWT tokens with 24-hour expiration
- BCrypt password hashing (strength 12)
- Token refresh mechanism
- Secure cookie options (HttpOnly, Secure, SameSite)

### Authorization
- Role-based access control (RBAC)
- Method-level security with @PreAuthorize
- Resource ownership checks

### Data Protection
- SQL injection prevention (JPA parameterized queries)
- XSS protection (React auto-escaping)
- CSRF protection (disabled for stateless JWT API)
- HTTPS enforcement (Nginx)
- CORS configuration (specific origins only)

### API Security
- Rate limiting (Redis-based)
- Request size limits
- Input validation (@Valid annotations)
- Error message sanitization

## Deployment Architecture

### Development
```
localhost:3000 (Frontend) → localhost:8080 (Backend) → localhost:5432 (PostgreSQL) + localhost:6379 (Redis)
```

### Production (AWS EC2)
```
Internet
    ↓
CloudFlare/CDN (optional)
    ↓
AWS Elastic IP
    ↓
Nginx (Port 80/443)
    ↓
Docker Compose:
  - Frontend container (Port 3000)
  - Backend container (Port 8080)
  - PostgreSQL container (Port 5432)
  - Redis container (Port 6379)
```

### Container Architecture

```
docker-compose.yml
    ├── codereview-frontend (nginx:alpine)
    ├── codereview-backend (eclipse-temurin:17-jre-alpine)
    ├── codereview-db (postgres:15-alpine)
    └── codereview-redis (redis:7-alpine)

Networks:
  - codereview-network (bridge)

Volumes:
  - postgres_data (persistent)
```

## Scalability Considerations

### Horizontal Scaling
- Stateless backend (JWT tokens)
- Redis for session sharing
- Database connection pooling (HikariCP)
- Load balancer ready (Nginx upstream)

### Vertical Scaling
- JVM tuning (-Xmx, -Xms)
- Database query optimization
- Redis caching strategy
- WebSocket connection limits

### Performance Optimizations
- Database indexes on frequently queried columns
- Redis caching for leaderboard and reviews
- Lazy loading for large code files
- Pagination for lists
- WebSocket message throttling
- Frontend code splitting

## Monitoring & Logging

### Application Logs
- Spring Boot logging (SLF4J + Logback)
- Log levels: INFO (production), DEBUG (development)
- Log rotation (10MB files, 30 days retention)

### Metrics
- Spring Boot Actuator endpoints
- Database connection pool metrics
- Redis connection metrics
- API response times

### Health Checks
- `/actuator/health` endpoint
- Database connectivity
- Redis connectivity
- Disk space

### Alerts (Optional)
- CPU usage > 80%
- Memory usage > 80%
- Database connection pool exhaustion
- Error rate > 1%

## Backup & Recovery

### Database Backups
- Automated daily backups (2 AM)
- 7-day retention
- pg_dump to compressed files
- Stored in /opt/backups/

### Application Backups
- Docker volume backups
- Configuration file backups
- SSL certificates backup

### Disaster Recovery
- Restore from latest backup
- Rebuild Docker containers
- Verify data integrity
- Test all functionality

## Future Enhancements

1. **Microservices**: Split into separate services
2. **Kubernetes**: Container orchestration
3. **Elasticsearch**: Full-text search
4. **GraphQL**: Alternative API
5. **Mobile App**: React Native
6. **CI/CD**: Advanced pipelines with testing stages
7. **Monitoring**: Prometheus + Grafana
8. **CDN**: CloudFlare for static assets
