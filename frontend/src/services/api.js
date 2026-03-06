import axios from 'axios'

/**
 * API Service — Axios HTTP client with JWT interceptors.
 * Skills: JavaScript, RESTful API, Full-Stack Web Development
 */

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// ── Axios instance ────────────────────────────────────────────────────────
const api = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 15_000, // 15 s
})

// ── Request interceptor — attach JWT ──────────────────────────────────────
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) config.headers.Authorization = `Bearer ${token}`
        return config
    },
    (error) => Promise.reject(error)
)

// ── Response interceptor — handle 401 globally ───────────────────────────
api.interceptors.response.use(
    (response) => response.data,   // unwrap .data so callers get the payload directly
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            // Clear Zustand store key as well
            localStorage.removeItem('auth-storage')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

// ── Authentication ────────────────────────────────────────────────────────
export const authAPI = {
    login: (credentials) => api.post('/api/auth/login', credentials),
    register: (userData) => api.post('/api/auth/register', userData),
    me: () => api.get('/api/auth/me'),
    logout: () => api.post('/api/auth/logout'),
}

// ── Reviews ───────────────────────────────────────────────────────────────
export const reviewAPI = {
    getAll: (params = {}) => api.get('/api/reviews', { params }),
    getById: (id) => api.get(`/api/reviews/${id}`),
    create: (data) => api.post('/api/reviews', data),
    update: (id, data) => api.put(`/api/reviews/${id}`, data),
    delete: (id) => api.delete(`/api/reviews/${id}`),
    updateStatus: (id, status) => api.patch(`/api/reviews/${id}/status`, { status }),
    getComments: (id) => api.get(`/api/reviews/${id}/comments`),
    triggerAnalysis: (id) => api.post(`/api/reviews/${id}/analyze`),
    search: (term) => api.get('/api/reviews', { params: { search: term } }),
    getRecent: (hours = 24) => api.get('/api/reviews/recent', { params: { hours } }),
}

// ── Comments ──────────────────────────────────────────────────────────────
export const commentAPI = {
    create: (reviewId, data) => api.post(`/api/reviews/${reviewId}/comments`, data),
    update: (id, commentText) => api.put(`/api/comments/${id}`, { commentText }),
    delete: (id) => api.delete(`/api/comments/${id}`),
    resolve: (id) => api.patch(`/api/comments/${id}/resolve`),
    unresolve: (id) => api.patch(`/api/comments/${id}/unresolve`),
    getByLine: (reviewId, lineNumber) => api.get(`/api/reviews/${reviewId}/comments/line/${lineNumber}`),
    unresolved: (reviewId) => api.get(`/api/reviews/${reviewId}/comments/unresolved`),
}

// ── Users ─────────────────────────────────────────────────────────────────
export const userAPI = {
    getProfile: () => api.get('/api/users/profile'),
    updateProfile: (data) => api.put('/api/users/profile', data),
    changePassword: (data) => api.post('/api/users/change-password', data),
    getLeaderboard: () => api.get('/api/users/leaderboard'),
    search: (q) => api.get('/api/users/search', { params: { q } }),
    getById: (id) => api.get(`/api/users/${id}`),
    getStats: (id) => api.get(`/api/users/${id}/stats`),
}

// ── Analytics ─────────────────────────────────────────────────────────────
export const analyticsAPI = {
    getDashboard: () => api.get('/api/analytics/dashboard'),
    getReviewMetrics: (id) => api.get(`/api/analytics/reviews/${id}`),
    getTrends: (days = 7) => api.get('/api/analytics/trends', { params: { days } }),
    getUserAnalytics: (id) => api.get(`/api/analytics/users/${id}`),
}

export default api
