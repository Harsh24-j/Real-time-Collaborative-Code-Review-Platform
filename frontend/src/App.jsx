import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './hooks/useAuthStore'

// Layout
import Navbar from './components/common/Navbar'
import MouseGlow from './components/common/MouseGlow'

// Auth pages
import LoginForm from './components/auth/LoginForm'
import RegisterForm from './components/auth/RegisterForm'
import UserProfile from './components/auth/UserProfile'

// Main pages
import Dashboard from './components/dashboard/Dashboard'
import Leaderboard from './components/dashboard/Leaderboard'
import ReviewList from './components/review/ReviewList'
import ReviewDetail from './components/review/ReviewDetail'
import CreateReview from './components/review/CreateReview'

/**
 * ProtectedRoute â€” redirects to /login when not authenticated.
 */
function ProtectedRoute({ children }) {
    const { isAuthenticated } = useAuthStore()
    return isAuthenticated ? children : <Navigate to="/login" replace />
}

/**
 * GuestRoute â€” redirects to /dashboard when already authenticated.
 */
function GuestRoute({ children }) {
    const { isAuthenticated } = useAuthStore()
    return !isAuthenticated ? children : <Navigate to="/dashboard" replace />
}

/**
 * App â€” root routing and layout shell.
 * Skills: JavaScript, Front-End Web Development, Model View Controller (View)
 */
function App() {
    return (
        <div className="min-h-screen text-slate-100">
            <MouseGlow />
            <Navbar />

            <main className="container mx-auto px-4 py-8 max-w-7xl">
                <Routes>
                    {/* Guest-only routes */}
                    <Route path="/login" element={<GuestRoute><LoginForm /></GuestRoute>} />
                    <Route path="/register" element={<GuestRoute><RegisterForm /></GuestRoute>} />

                    {/* Protected routes */}
                    <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
                    <Route path="/reviews" element={<ProtectedRoute><ReviewList /></ProtectedRoute>} />
                    <Route path="/reviews/create" element={<ProtectedRoute><CreateReview /></ProtectedRoute>} />
                    <Route path="/reviews/:id" element={<ProtectedRoute><ReviewDetail /></ProtectedRoute>} />
                    <Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><UserProfile /></ProtectedRoute>} />

                    {/* Default redirect */}
                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                </Routes>
            </main>
        </div>
    )
}

export default App
