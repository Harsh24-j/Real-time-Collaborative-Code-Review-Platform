import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { UserPlus, Eye, EyeOff, Code2, CheckCircle, XCircle } from 'lucide-react'
import { authAPI } from '../../services/api'

/**
 * RegisterForm Component
 * Skills: JavaScript, HTML/CSS, Responsive Web Design, Front-End Web Development
 */
function RegisterForm() {
    const navigate = useNavigate()

    const [formData, setFormData] = useState({
        fullName: '', username: '', email: '', password: ''
    })
    const [showPassword, setShowPassword] = useState(false)
    const [loading, setLoading] = useState(false)

    // Password strength checks
    const checks = {
        length: formData.password.length >= 8,
        uppercase: /[A-Z]/.test(formData.password),
        lowercase: /[a-z]/.test(formData.password),
        digit: /\d/.test(formData.password),
    }
    const passwordStrong = Object.values(checks).every(Boolean)

    const handleChange = (e) =>
        setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }))

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!passwordStrong) {
            toast.error('Please meet all password requirements')
            return
        }
        setLoading(true)
        try {
            await authAPI.register(formData)
            toast.success('Account created! Please sign in.')
            navigate('/login')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Registration failed')
        } finally {
            setLoading(false)
        }
    }

    const Check = ({ ok, label }) => (
        <li className={`flex items-center gap-1.5 text-xs transition-colors ${ok ? 'text-emerald-400' : 'text-slate-500'
            }`}>
            {ok
                ? <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />
                : <XCircle className="w-3.5 h-3.5 flex-shrink-0" />}
            {label}
        </li>
    )

    return (
        <div className="min-h-[85vh] flex items-center justify-center px-4 py-8">
            <div className="w-full max-w-md animate-slide-up">

                {/* Header */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl
                          bg-blue-600/20 border border-blue-500/30 mb-4">
                        <Code2 className="w-8 h-8 text-blue-400" />
                    </div>
                    <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Create account</h1>
                    <p className="text-slate-400 mt-2 text-sm">Join the CodeReview Platform</p>
                </div>

                <div className="card">
                    <form onSubmit={handleSubmit} className="space-y-5">

                        {/* Full Name */}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Full Name
                            </label>
                            <input
                                id="fullName"
                                type="text"
                                name="fullName"
                                value={formData.fullName}
                                onChange={handleChange}
                                className="input"
                                placeholder="Jane Doe"
                                autoComplete="name"
                                required
                            />
                        </div>

                        {/* Username */}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Username
                            </label>
                            <input
                                id="reg-username"
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className="input"
                                placeholder="jane_doe (letters, digits, underscores)"
                                autoComplete="username"
                                required
                            />
                        </div>

                        {/* Email */}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Email
                            </label>
                            <input
                                id="email"
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="input"
                                placeholder="jane@example.com"
                                autoComplete="email"
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
                                    id="reg-password"
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="input pr-11"
                                    placeholder="••••••••"
                                    autoComplete="new-password"
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

                            {/* Live password strength indicators */}
                            {formData.password.length > 0 && (
                                <ul className="mt-2 grid grid-cols-2 gap-1">
                                    <Check ok={checks.length} label="At least 8 characters" />
                                    <Check ok={checks.uppercase} label="One uppercase letter" />
                                    <Check ok={checks.lowercase} label="One lowercase letter" />
                                    <Check ok={checks.digit} label="One number" />
                                </ul>
                            )}
                        </div>

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={loading || !passwordStrong}
                            className="btn btn-primary btn-lg w-full mt-2"
                        >
                            {loading ? (
                                <><span className="spinner" aria-hidden="true" /> Creating account…</>
                            ) : (
                                <><UserPlus className="w-4 h-4" /> Create Account</>
                            )}
                        </button>
                    </form>

                    <div className="divider" />

                    <p className="text-center text-sm text-slate-400">
                        Already have an account?{' '}
                        <Link to="/login"
                            className="text-blue-400 hover:text-blue-300 font-medium transition-colors">
                            Sign in
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    )
}

export default RegisterForm
