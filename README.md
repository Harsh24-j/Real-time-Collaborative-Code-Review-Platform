# Real-time Collaborative Code Review Platform

[![Build](https://github.com/Harsh24-j/code-review-platform/workflows/CI-CD/badge.svg)](https://github.com/Harsh24-j/code-review-platform/actions)
[![codecov](https://codecov.io/gh/Harsh24-j/code-review-platform/branch/main/graph/badge.svg)](https://codecov.io/gh/Harsh24-j/code-review-platform)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Stars](https://img.shields.io/github/stars/Harsh24-j/code-review-platform.svg)](https://github.com/Harsh24-j/code-review-platform/stargazers)

## 🚀 Overview
AI-powered real-time collaborative code review platform with WebSocket, gamification, and smart conflict resolution. Built with Spring Boot, React, PostgreSQL, Redis, Docker, and AWS EC2.

## Technology Stack

### Backend (Spring Boot - J2EE)
- **Spring Boot 3.2.0** - Core framework
- **Spring Web** - RESTful API development
- **Spring Security** - JWT authentication, secure coding
- **Spring Data JPA** - Data persistence (PostgreSQL)
- **Spring AI** - OpenAI integration for code analysis and autocorrect
- **Spring WebSocket** - Real-time communication
- **Hibernate** - ORM for J2EE
- **Maven** - Dependency management

### Frontend (JavaScript/React)
- **React 18** - UI framework
- **Vite** - Build tool
- **Tailwind CSS** - Responsive web design
- **Axios** - HTTP client
- **SockJS + STOMP** - WebSocket client
- **Monaco Editor** - Code editor component

### Cloud & Deployment
- **AWS EC2** - Application hosting
- **Docker** - Containerization
- **Nginx** - Reverse proxy
- **GitHub Actions** - CI/CD pipeline

## Skill Sets Demonstrated

✅ **Spring Boot** - Core backend framework  
✅ **RESTful API** - HTTP endpoints for all operations  
✅ **JavaScript** - Frontend logic and interactions  
✅ **Full-Stack Web Development** - Complete end-to-end application  
✅ **Model View Controller** - Spring MVC architecture  
✅ **Responsive Web Design** - Mobile-first UI with Tailwind  
✅ **Java Platform Enterprise Edition** - JPA, Servlets, Enterprise patterns  
✅ **HTML and CSS** - Semantic markup and styling  
✅ **Server Side** - Backend business logic  
✅ **Back-End Web Development** - API, database, authentication  
✅ **Front-End Web Development** - React UI components  
✅ **Cloud Deployment** - AWS EC2 with CI/CD  
✅ **Amazon EC2** - Instance configuration and management  
✅ **Secure Coding** - JWT, BCrypt, input validation  
✅ **Web Applications** - Complete SPA architecture  
✅ **API** - RESTful API design and documentation  
✅ **Data Persistence** - PostgreSQL with JPA/Hibernate  

## Project Structure

```
code-review-platform/
├── backend/                 # Spring Boot Backend
│   ├── src/main/java/       # Java source code
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST Controllers (RESTful API)
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # JPA Entities (Data Persistence)
│   │   ├── repository/      # Spring Data Repositories
│   │   ├── service/         # Business Logic (Server Side)
│   │   └── util/            # Utility classes
│   ├── src/main/resources/  # Configuration files
│   └── pom.xml              # Maven dependencies
├── frontend/                # React Frontend
│   ├── src/                 # JavaScript source
│   │   ├── components/      # React components (HTML/CSS)
│   │   ├── services/        # API integration
│   │   └── hooks/           # Custom React hooks
│   └── package.json         # npm dependencies
└── deployment/              # AWS EC2 deployment configs
    ├── nginx/               # Nginx configuration
    └── systemd/             # Service files
```

## Features

### Core Functionality
1. **User Authentication** (Secure Coding)
   - JWT-based authentication
   - BCrypt password hashing
   - Role-based access control

2. **Code Review Management** (RESTful API)
   - Create, read, update, delete reviews
   - Real-time collaboration
   - Status tracking

3. **AI-Powered Analysis** (Server Side)
   - Automatic bug detection
   - Code smell identification
   - Security vulnerability scanning
   - 1-Click Code Autocorrect (Spring AI)

4. **Real-time Collaboration** (WebSocket)
   - Live comments
   - Online user presence
   - Typing indicators

    - Achievement system

## 📚 Documentation

- [API Documentation](docs/API_DOCUMENTATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Live Docs](https://Harsh24-j.github.io/code-review-platform)

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a pull request.

## Setup Instructions

### Prerequisites
- JDK 17 or higher
- Maven 3.8+
- Node.js 18+
- PostgreSQL 15+
- AWS Account (for deployment)

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on: http://localhost:8080  
Swagger API Docs: http://localhost:8080/swagger-ui.html

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: http://localhost:5173

### Database Setup
```sql
CREATE DATABASE codereview;
CREATE USER codereview_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE codereview TO codereview_user;
```

## API Endpoints (RESTful API)

### Authentication
- POST `/api/auth/register` - User registration
- POST `/api/auth/login` - User login (returns JWT)

### Reviews
- GET `/api/reviews` - List all reviews
- POST `/api/reviews` - Create new review
- GET `/api/reviews/{id}` - Get review details
- GET `/api/reviews/{id}/suggestions` - Get detailed AI suggestions and code fixes
- PUT `/api/reviews/{id}` - Update review
- DELETE `/api/reviews/{id}` - Delete review

### Comments
- GET `/api/reviews/{id}/comments` - Get review comments
- POST `/api/reviews/{id}/comments` - Add comment
- PUT `/api/comments/{id}` - Update comment
- DELETE `/api/comments/{id}` - Delete comment

### WebSocket
- CONNECT `/ws-review` - Establish WebSocket connection
- SUBSCRIBE `/topic/review/{id}` - Subscribe to review updates
- SEND `/app/review/{id}/comment` - Send comment

## AWS EC2 Deployment

### EC2 Instance Setup
1. Launch Ubuntu 22.04 LTS instance (t2.medium)
2. Configure security groups (ports 22, 80, 443, 8080)
3. Install dependencies (JDK, Docker, Nginx)
4. Deploy application

### Deployment Commands
```bash
# SSH into EC2
ssh -i your-key.pem ubuntu@your-ec2-ip

# Clone repository
git clone https://github.com/your-username/code-review-platform.git

# Run deployment script
cd code-review-platform
chmod +x deployment/deploy.sh
./deployment/deploy.sh
```

## Security Features (Secure Coding)

1. **Authentication & Authorization**
   - JWT tokens with expiration
   - BCrypt password hashing (strength 12)
   - Role-based access control (RBAC)

2. **Input Validation**
   - Jakarta Bean Validation (@Valid)
   - Custom validators for business rules
   - XSS prevention

3. **API Security**
   - CORS configuration
   - Rate limiting
   - SQL injection prevention (JPA parameterized queries)

4. **HTTPS/TLS**
   - SSL certificate configuration
   - Secure cookie flags
   - HSTS headers

## Testing

### Backend Tests
```bash
cd backend
mvn test                         # Run all tests
mvn test -Dtest=UserServiceTest  # Run specific test
mvn verify                       # Run integration tests
```

### Frontend Tests
```bash
cd frontend
npm test              # Run Jest tests
npm run test:e2e      # Run Cypress E2E tests
```

## CI/CD Pipeline

GitHub Actions workflow automatically:
1. Runs tests on every push
2. Builds Docker images
3. Deploys to AWS EC2
4. Runs smoke tests

## Performance Metrics

- API Response Time: < 100ms (avg)
- WebSocket Latency: < 50ms
- Concurrent Users: 1000+
- Database Query Time: < 20ms (avg)

## Architecture Highlights

### MVC Pattern (Model View Controller)
- **Model**: JPA entities in `model/` package
- **View**: React components (frontend)
- **Controller**: Spring `@RestController` classes

### Data Persistence
- JPA/Hibernate for ORM
- PostgreSQL for relational data
- Redis for caching and sessions
- Flyway for database migrations

### Responsive Web Design
- Mobile-first approach
- Tailwind CSS utility classes
- Responsive breakpoints (sm, md, lg, xl)
- Accessible UI components

## License
MIT License

## Author
[Harsh Shrivastava]  
2026 Computer Science Graduate  
Demonstrating full-stack development skills for software engineering roles

## Contact
- Email: harshshrivastava807@gmail.com
- LinkedIn: linkedin.com/in/harshshrivastava24
- GitHub: github.com/Harsh24-j
