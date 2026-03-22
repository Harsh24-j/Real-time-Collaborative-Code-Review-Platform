import axios from 'axios'
import toast from 'react-hot-toast'

/**
 * API Service â€” Axios HTTP client with JWT interceptors and structured error handling.
 * Skills: JavaScript, RESTful API, Full-Stack Web Development
 *
 * Error shape from backend GlobalExceptionHandler:
 * { status, error, message, path, timestamp }
 */

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// â”€â”€ Axios instance â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
const api = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
    timeout: 30_000,   // 30 s (AI analysis can be slow)
})

// â”€â”€ Request interceptor â€” attach JWT â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) config.headers.Authorization = `Bearer ${token}`
        return config
    },
    (error) => Promise.reject(error)
)

// â”€â”€ Response interceptor â€” global error handling â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
api.interceptors.response.use(
    // Success: unwrap `.data` so callers get the payload directly
    (response) => response.data,

    (error) => {
        const status = error.response?.status
        const data = error.response?.data   // our ErrorResponse DTO
        const message = data?.message || error.message || 'An unexpected error occurred'

        // 401 â€” session expired or not logged in â†’ auto logout
        if (status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('auth-storage')
            // Only redirect if not already on an auth page
            if (!window.location.pathname.startsWith('/login') &&
                !window.location.pathname.startsWith('/register')) {
                window.location.href = '/login'
            }
            return Promise.reject(error)
        }

        // 403 â€” access denied â€” let the component handle it, just propagate
        if (status === 403) {
            return Promise.reject(error)
        }

        // 404 â€” let the component decide how to surface it
        if (status === 404) {
            return Promise.reject(error)
        }

        // 409 â€” conflict (duplicate email/username etc.)
        if (status === 409) {
            return Promise.reject(error)
        }

        // 422 / 400 validation â€” components handle field errors
        if (status === 400 || status === 422) {
            return Promise.reject(error)
        }

        // 503 / 500+ â€” server errors â€” show global toast
        if (!status || status >= 500) {
            const toastMsg = status === 503
                ? 'AI service is temporarily unavailable. Please try again shortly.'
                : 'Server error. Please try again.'
            toast.error(toastMsg, { id: 'server-error', duration: 5000 })
            return Promise.reject(error)
        }

        // Network / timeout errors
        if (!error.response) {
            if (error.code === 'ECONNABORTED') {
                toast.error('Request timed out. The server is taking too long to respond.', {
                    id: 'timeout-error',
                })
            } else {
                toast.error('Network error. Check your connection.', { id: 'network-error' })
            }
        }

        return Promise.reject(error)
    }
)

// â”€â”€ Error utility helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Extract a human-readable error message from an Axios error.
 * Falls back through: backend message â†’ network message â†’ generic.
 */
export function getErrorMessage(error) {
    return error?.response?.data?.message
        || error?.message
        || 'Something went wrong'
}

/**
 * Extract backend validation field errors as a flat object.
 * Backend sends: "fullName: must not be blank; email: must be a valid email"
 * Returns: { fullName: "must not be blank", email: "must be a valid email" }
 */
export function getFieldErrors(error) {
    const msg = error?.response?.data?.message || ''
    if (!msg.includes(':')) return {}
    return Object.fromEntries(
        msg.split(';').map(part => {
            const [field, ...rest] = part.trim().split(':')
            return [field?.trim(), rest.join(':').trim()]
        }).filter(([k]) => k)
    )
}

// â”€â”€ Authentication â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const authAPI = {
    login: (credentials) => api.post('/api/auth/login', credentials),
    register: (userData) => api.post('/api/auth/register', userData),
    me: () => api.get('/api/auth/me'),
    logout: () => api.post('/api/auth/logout'),
}

// â”€â”€ Reviews â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const reviewAPI = {
    getAll: (params = {}) => api.get('/api/reviews', { params }),
    getById: (id) => api.get(`/api/reviews/${id}`),
    create: (data) => api.post('/api/reviews', data),
    update: (id, data) => api.put(`/api/reviews/${id}`, data),
    delete: (id) => api.delete(`/api/reviews/${id}`),
    updateStatus: (id, status) => api.patch(`/api/reviews/${id}/status`, { status }),
    getComments: (id) => api.get(`/api/reviews/${id}/comments`),
    triggerAnalysis: (id) => api.post(`/api/reviews/${id}/analyze`),
    getAiSuggestions: (id) => api.get(`/api/reviews/${id}/suggestions`),
    search: (term) => api.get('/api/reviews', { params: { search: term } }),
    getRecent: (hours = 24) => api.get('/api/reviews/recent', { params: { hours } }),
}

// â”€â”€ Comments â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const commentAPI = {
    create: (reviewId, data) => api.post(`/api/reviews/${reviewId}/comments`, data),
    update: (id, commentText) => api.put(`/api/comments/${id}`, { commentText }),
    delete: (id) => api.delete(`/api/comments/${id}`),
    resolve: (id) => api.patch(`/api/comments/${id}/resolve`),
    unresolve: (id) => api.patch(`/api/comments/${id}/unresolve`),
    getByLine: (reviewId, lineNumber) =>
        api.get(`/api/reviews/${reviewId}/comments/line/${lineNumber}`),
    unresolved: (reviewId) => api.get(`/api/reviews/${reviewId}/comments/unresolved`),
}

// â”€â”€ Users â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const userAPI = {
    getProfile: () => api.get('/api/users/profile'),
    updateProfile: (data) => api.put('/api/users/profile', data),
    changePassword: (data) => api.post('/api/users/change-password', data),
    getLeaderboard: () => api.get('/api/users/leaderboard'),
    search: (q) => api.get('/api/users/search', { params: { q } }),
    getById: (id) => api.get(`/api/users/${id}`),
    getStats: (id) => api.get(`/api/users/${id}/stats`),
}

// â”€â”€ Analytics â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
export const analyticsAPI = {
    getDashboard: () => api.get('/api/analytics/dashboard'),
    getReviewMetrics: (id) => api.get(`/api/analytics/reviews/${id}`),
    getTrends: (days = 7) => api.get('/api/analytics/trends', { params: { days } }),
    getUserAnalytics: (id) => api.get(`/api/analytics/users/${id}`),
}

export default api
