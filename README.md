# Zyncora 🚀

**Real-time collaborative code review platform powered by AI.**

Zyncora helps developers review code together in real time, with AI-assisted code analysis, authenticated collaboration, and a production-style deployment pipeline.

## 🌐 Demo

**Live application:** https://codereview-frontend-ttjg.onrender.com

## ✨ Highlights

- **Real-time collaboration:** WebSocket-based synchronization for shared code review sessions
- **AI code analysis:** GPT-4 integration that generates severity-based suggestions
- **Secure access:** JWT-based authentication
- **Code editor:** Monaco Editor integration
- **Gamification:** Points and badges to encourage review participation
- **Responsive UI:** React-based interface for desktop and mobile layouts

## 🏗️ Architecture

```text
React + Vite + Tailwind CSS
            |
            | HTTP / WebSocket
            v
     Spring Boot Backend
        /      |       \
       /       |        \
PostgreSQL   Redis    GPT-4 API
```

**Backend:** Spring Boot 3.2, PostgreSQL, Redis, WebSocket  
**Frontend:** React 18, Vite, Tailwind CSS, Monaco Editor  
**DevOps:** Docker, Render.com, GitHub Actions

## 📊 Project Metrics

The project is documented with the following measured targets/results:

- **<500 ms** response time
- **100+** concurrent users
- **80%+** test coverage
- **99.9%** uptime

> For interviews and technical discussions, the measurement method and test environment for these metrics should be treated as part of the project documentation.

## 🔍 Engineering Focus

### Real-time collaboration
WebSocket communication keeps collaborative review sessions synchronized without requiring constant polling.

### AI-assisted review
GPT-4 is integrated into the review workflow to generate code-analysis suggestions categorized as **CRITICAL**, **WARNING**, and **INFO**.

### Persistence and performance
PostgreSQL provides durable application data storage while Redis is used as part of the application's real-time/performance architecture.

### Deployment and delivery
The project uses Docker and GitHub Actions for repeatable builds and automated CI/CD, with deployment on Render.com.

## 🧪 Testing

The project includes unit and integration testing with a reported **80%+ test coverage**.

For reproducible evaluation, document the exact test command, coverage tool, and environment used to produce the reported coverage and performance figures.

## 📁 Repository

The codebase is organized around a Spring Boot backend and React frontend, with the main application concerns separated into backend services, persistence, real-time communication, and UI components.

## 👨‍💻 Author

**Harsh Shrivastava**  
GitHub: https://github.com/Harsh24-j

---

*Sync your code, sync your team.*
