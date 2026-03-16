# API Documentation

Base URL:
- Development: `http://localhost:8080`
- Production: `https://your-domain.com`

## Authentication

All endpoints (except login/register) require JWT token:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## Endpoints

### Authentication

#### POST /api/auth/register
Register a new user

**Request:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string"
}
```

**Response:** `201 Created`
```json
{
  "message": "User registered successfully"
}
```

#### POST /api/auth/login
Login user

**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:** `200 OK`
```json
{
  "token": "jwt.token.here",
  "user": {
    "id": 1,
    "username": "string",
    "email": "string",
    "fullName": "string",
    "points": 0,
    "badges": []
  }
}
```

#### GET /api/auth/me
Get current user info

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "points": 100,
  "badges": ["CODE_REVIEWER", "EARLY_ADOPTER"]
}
```

### Reviews

#### GET /api/reviews
Get all reviews

**Query Parameters:**
- `status` (optional): OPEN, IN_REVIEW, APPROVED, REJECTED
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Fix authentication bug",
    "description": "Review my auth fix",
    "status": "OPEN",
    "language": "javascript",
    "creator": {
      "id": 1,
      "username": "john"
    },
    "createdAt": "2026-03-16T10:00:00Z",
    "commentCount": 5,
    "qualityScore": 85.5
  }
]
```

#### POST /api/reviews
Create a new review

**Request:**
```json
{
  "title": "string",
  "description": "string",
  "codeContent": "string",
  "language": "string",
  "repositoryUrl": "string (optional)"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "string",
  "status": "OPEN",
  "codeContent": "string",
  "language": "string"
}
```

#### GET /api/reviews/{id}
Get review by ID

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "codeContent": "string",
  "language": "javascript",
  "status": "OPEN",
  "creator": {...},
  "comments": [...],
  "aiSuggestions": [...],
  "createdAt": "2026-03-16T10:00:00Z"
}
```

#### PATCH /api/reviews/{id}/status
Update review status

**Request:**
```json
{
  "status": "APPROVED"
}
```

**Response:** `200 OK`

#### DELETE /api/reviews/{id}
Delete a review

**Response:** `204 No Content`

### Comments

#### POST /api/reviews/{reviewId}/comments
Add a comment

**Request:**
```json
{
  "commentText": "string",
  "lineNumber": 10
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "commentText": "string",
  "lineNumber": 10,
  "user": {...},
  "isResolved": false,
  "createdAt": "2026-03-16T10:30:00Z"
}
```

#### GET /api/reviews/{reviewId}/comments
Get all comments for a review

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "commentText": "Great code!",
    "lineNumber": 5,
    "user": {...},
    "isResolved": false,
    "createdAt": "2026-03-16T10:30:00Z"
  }
]
```

#### PATCH /api/comments/{id}/resolve
Mark comment as resolved

**Response:** `200 OK`

### Users

#### GET /api/users/profile
Get current user profile

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "fullName": "John Doe",
  "points": 150,
  "badges": ["CODE_REVIEWER", "HELPFUL"],
  "reviewsCreated": 10,
  "commentsGiven": 50,
  "rank": 5
}
```

#### GET /api/users/leaderboard
Get top users by points

**Response:** `200 OK`
```json
[
  {
    "username": "john",
    "fullName": "John Doe",
    "points": 500,
    "badges": [...]
  }
]
```

### Analytics

#### GET /api/analytics/dashboard
Get dashboard statistics

**Response:** `200 OK`
```json
{
  "totalReviews": 100,
  "totalComments": 500,
  "avgQualityScore": 82.5,
  "activeUsers": 50
}
```

## WebSocket

### Connect

```javascript
const socket = new SockJS('http://localhost:8080/ws-review')
const stompClient = Stomp.over(socket)

stompClient.connect({}, frame => {
  console.log('Connected:', frame)
})
```

### Subscribe to Review Updates

```javascript
stompClient.subscribe('/topic/review/1', message => {
  const update = JSON.parse(message.body)
  console.log('Review update:', update)
})
```

### Send Comment

```javascript
stompClient.send('/app/review/1/comment', {}, JSON.stringify({
  commentText: 'Great code!',
  lineNumber: 10
}))
```

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2026-03-16T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/reviews"
}
```

Common status codes:
- `400 Bad Request`: Invalid input
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Rate Limiting

- 100 requests per minute per user
- Returns `429 Too Many Requests` when exceeded
- Retry after header provided
