import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { LogIn, Eye, EyeOff, Code2 } from 'lucide-react'
import { authAPI } from '../../services/api'
import { useAuthStore } from '../../hooks/useAuthStore'

/**
 * LoginForm Component
 * Skills: JavaScript, HTML/CSS, Responsive Web Design, Front-End Web Development
 */
function LoginForm() {
    const navigate = useNavigate()
    const { login } = useAuthStore()
    const [showPassword, setShowPassword] = useState(false)
    const [loading, setLoading] = useState(false)
    const [formData, setFormData] = useState({ username: '', password: '' })

    const handleChange = (e) =>
        setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }))

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        try {
            const data = await authAPI.login(formData)
            // AuthResponse contains token + user fields
            login(data.token, {
                id: data.userId,
                username: data.username,
                email: data.email,
                fullName: data.fullName,
                role: data.role,
                points: data.points,
            })
            toast.success(`Welcome back, ${data.username}! 👋`)
            navigate('/dashboard')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Login failed — check your credentials')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-[85vh] flex items-center justify-center px-4">
            <div className="w-full max-w-md animate-slide-up">

                {/* Logo / brand header */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl
                          bg-blue-600/20 border border-blue-500/30 mb-4">
                        <Code2 className="w-8 h-8 text-blue-400" />
                    </div>
                    <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Welcome back</h1>
                    <p className="text-slate-400 mt-2 text-sm">Sign in to your CodeReview account</p>
                </div>

                <div className="card">
                    <form onSubmit={handleSubmit} className="space-y-5">

                        {/* Username */}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Username
                            </label>
                            <input
                                id="username"
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className="input"
                                placeholder="your_username"
                                autoComplete="username"
                                required
                            />
                        </div>

                        {/* Password */}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Password
                            </label>
                            <div className="relative">
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="input pr-11"
                                    placeholder="••••••••"
                                    autoComplete="current-password"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((p) => !p)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2
                             text-slate-500 hover:text-slate-300 transition-colors"
                                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                                >
                                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>
                        </div>

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="btn btn-primary btn-lg w-full mt-2"
                        >
                            {loading ? (
                                <><span className="spinner" aria-hidden="true" /> Signing in…</>
                            ) : (
                                <><LogIn className="w-4 h-4" /> Sign In</>
                            )}
                        </button>
                    </form>

                    <div className="divider" />

                    <p className="text-center text-sm text-slate-400">
                        Don't have an account?{' '}
                        <Link to="/register"
                            className="text-blue-400 hover:text-blue-300 font-medium transition-colors">
                            Create one
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    )
}

export default LoginForm
