import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { authAPI } from '../../services/api'
import { useAuthStore } from '../../hooks/useAuthStore'
import { LogIn, Eye, EyeOff, Code2 } from 'lucide-react'
import BackgroundWrapper from '../common/BackgroundWrapper'
import Typewriter from '../common/Typewriter'
import FloatingOrbs from '../common/FloatingOrbs'

function LoginForm() {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false) // Changed from 'loading' to 'isLoading'

  // Personalized greeting based on local time
  const hour = new Date().getHours()
  let greeting = 'Good Evening'
  if (hour >= 5 && hour < 12) greeting = 'Good Morning'
  else if (hour >= 12 && hour < 17) greeting = 'Good Afternoon'
  else greeting = 'Good Evening'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true) // Changed from 'setLoading' to 'setIsLoading'

    try {
      const data = await authAPI.login(formData)
      login(data.token, {
          id: data.userId,
          username: data.username,
          email: data.email,
          fullName: data.fullName,
          role: data.role,
          points: data.points,
      })
      toast.success('🎉 Welcome back!')
      navigate('/dashboard')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Login failed')
    } finally {
      setIsLoading(false) // Changed from 'setLoading' to 'setIsLoading'
    }
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

    return (
        <BackgroundWrapper variant="code1">
            <FloatingOrbs />
            <div className="min-h-screen flex items-center justify-center px-4 py-12">
                <div className="glass-card-dark w-full max-w-md p-8 rounded-2xl relative z-10 animate-slide-up shadow-2xl border border-white/10">
                    
                    {/* Header */}
                    <div className="text-center mb-10">
                        <div className="w-16 h-16 bg-gradient-to-tr from-blue-500 to-purple-500 rounded-2xl mx-auto flex items-center justify-center mb-6 shadow-lg shadow-purple-500/30 transform transition-transform hover:scale-110 hover:rotate-3 duration-300">
                            <Code2 size={32} className="text-white" />
                        </div>
                        <h2 className="text-3xl font-bold text-white mb-2 tracking-tight h-10">
                            <Typewriter text={`${greeting}, Developer.`} speed={70} delay={300} />
                        </h2>
                        <p className="text-slate-400">
                            Sign in to continue your code reviews
                        </p>
                    </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-indigo-100 mb-1.5">
                Username
              </label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-purple-400/50"
                placeholder="Enter your username"
                required
              />
            </div>

            <div>
               <label className="block text-sm font-medium text-indigo-100 mb-1.5">
                  Password
               </label>
               <div className="relative">
                  <input
                     id="password"
                     type={showPassword ? 'text' : 'password'}
                     name="password"
                     value={formData.password}
                     onChange={handleChange}
                     className="input bg-white/5 text-white placeholder-indigo-300/40 border-indigo-400/20 focus:border-purple-400/50 pr-11"
                     placeholder="Enter your password"
                     required
                  />
                  <button
                     type="button"
                     onClick={() => setShowPassword((p) => !p)}
                     className="absolute right-3 top-1/2 -translate-y-1/2 mt-0.5 text-indigo-300/50 hover:text-indigo-200 transition-colors"
                     aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                     {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
               </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="btn btn-primary w-full disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden group mt-2 shadow-purple-500/25"
            >
              <span className="relative z-10 flex items-center justify-center font-semibold tracking-wide">
                <LogIn className="w-5 h-5 mr-2" />
                {isLoading ? 'Signing in...' : 'Sign In'}
              </span>
              <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-pink-500 transform scale-x-0 group-hover:scale-x-100 transition-transform origin-left duration-300" />
            </button>
          </form>

          <div className="mt-8 text-center border-t border-white/5 pt-6">
            <p className="text-indigo-200/60 text-sm">
              Don't have an account?{' '}
              <Link 
                to="/register" 
                className="text-purple-400 hover:text-pink-300 font-semibold transition-colors ml-1"
              >
                Create one
              </Link>
            </p>
          </div>
        </div>
      </div>
    </BackgroundWrapper>
  )
}

export default LoginForm
