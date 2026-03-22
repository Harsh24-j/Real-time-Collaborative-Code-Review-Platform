import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { UserPlus, Eye, EyeOff, Code2, CheckCircle, XCircle } from 'lucide-react'
import { authAPI } from '../../services/api'
import BackgroundWrapper from '../common/BackgroundWrapper'
import Typewriter from '../common/Typewriter'
import FloatingOrbs from '../common/FloatingOrbs'

function RegisterForm() {
    const navigate = useNavigate()

    const [formData, setFormData] = useState({
        fullName: '', username: '', email: '', password: ''
    })
    const [showPassword, setShowPassword] = useState(false)
    const [isLoading, setIsLoading] = useState(false)

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
        setIsLoading(true)
        try {
            await authAPI.register(formData)
            toast.success('Account created! Please sign in.')
            navigate('/login')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Registration failed')
        } finally {
            setIsLoading(false)
        }
    }

    const Check = ({ ok, label }) => (
        <li className={`flex items-center gap-1.5 text-xs transition-colors ${ok ? 'text-emerald-400 font-medium' : 'text-indigo-300/50'
            }`}>
            {ok
                ? <CheckCircle className="w-3.5 h-3.5 flex-shrink-0" />
                : <XCircle className="w-3.5 h-3.5 flex-shrink-0" />}
            {label}
        </li>
    )

    return (
        <BackgroundWrapper variant="register">
            <FloatingOrbs />
            <div className="min-h-screen flex items-center justify-center px-4 py-12">
                <div className="glass-card-dark w-full max-w-md p-8 rounded-2xl relative z-10 animate-slide-up shadow-2xl border border-white/10">

                    {/* Header */}
                    <div className="text-center mb-10">
                        <div className="w-16 h-16 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-2xl mx-auto flex items-center justify-center mb-6 shadow-lg shadow-purple-500/30 transform transition-transform hover:scale-110 hover:-rotate-3 duration-300">
                            <Code2 size={32} className="text-white" />
                        </div>
                        <h2 className="text-3xl font-bold text-white mb-2 tracking-tight h-10">
                            <Typewriter text="Initializing profile..." speed={60} delay={400} />
                        </h2>
                        <p className="text-slate-400 animate-fade-in animation-delay-2000">
                            Join the CodeReview Platform
                        </p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">

                        {/* Full Name */}
                        <div>
                            <label className="block text-sm font-medium text-indigo-100 mb-1">
                                Full Name
                            </label>
                            <input
                                id="fullName"
                                type="text"
                                name="fullName"
                                value={formData.fullName}
                                onChange={handleChange}
                                className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-blue-400/50 py-2.5"
                                placeholder="Jane Doe"
                                autoComplete="name"
                                required
                            />
                        </div>

                        {/* Username */}
                        <div>
                            <label className="block text-sm font-medium text-indigo-100 mb-1">
                                Username
                            </label>
                            <input
                                id="reg-username"
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-blue-400/50 py-2.5"
                                placeholder="jane_doe"
                                autoComplete="username"
                                required
                            />
                        </div>

                        {/* Email */}
                        <div>
                            <label className="block text-sm font-medium text-indigo-100 mb-1">
                                Email
                            </label>
                            <input
                                id="email"
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-blue-400/50 py-2.5"
                                placeholder="jane@example.com"
                                autoComplete="email"
                                required
                            />
                        </div>

                        {/* Password */}
                        <div>
                            <label className="block text-sm font-medium text-indigo-100 mb-1">
                                Password
                            </label>
                            <div className="relative">
                                <input
                                    id="reg-password"
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-blue-400/50 pr-11 py-2.5"
                                    placeholder="â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢"
                                    autoComplete="new-password"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((p) => !p)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-indigo-300/50 hover:text-indigo-200 transition-colors"
                                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                                >
                                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                </button>
                            </div>

                            {/* Live password strength indicators */}
                            {formData.password.length > 0 && (
                                <ul className="mt-3 grid grid-cols-2 gap-2 bg-black/20 p-3 rounded-lg border border-white/5">
                                    <Check ok={checks.length} label="At least 8 chars" />
                                    <Check ok={checks.uppercase} label="One uppercase" />
                                    <Check ok={checks.lowercase} label="One lowercase" />
                                    <Check ok={checks.digit} label="One number" />
                                </ul>
                            )}
                        </div>

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={isLoading || !passwordStrong}
                            className="btn btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden group mt-4 shadow-blue-500/25 py-3"
                        >
                            <span className="relative z-10 flex items-center justify-center font-semibold tracking-wide">
                                <UserPlus className="w-5 h-5 mr-2" />
                                {isLoading ? 'Creating...' : 'Create Account'}
                            </span>
                            <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-purple-600 transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left duration-300" />
                        </button>
                    </form>

                    <div className="mt-8 text-center border-t border-white/5 pt-6">
                        <p className="text-indigo-200/60 text-sm">
                            Already have an account?{' '}
                            <Link to="/login"
                                className="text-blue-400 hover:text-purple-300 font-semibold transition-colors ml-1">
                                Sign in
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </BackgroundWrapper>
    )
}

export default RegisterForm
