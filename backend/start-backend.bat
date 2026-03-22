@echo off
REM ── Local Dev Backend Starter ──────────────────────────────────────────
REM Copy this file, rename it start-backend.local.bat, and fill in your values.
REM Do NOT commit start-backend.local.bat (it's in .gitignore)

set DB_PASSWORD=CHANGE_ME
set OPENAI_API_KEY=CHANGE_ME
set JWT_SECRET=your-secret-key-must-be-at-least-256-bits-long-change-in-production
set ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:5174
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
