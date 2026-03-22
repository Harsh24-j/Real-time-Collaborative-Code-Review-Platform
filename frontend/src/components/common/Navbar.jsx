import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import {
    Code2, LayoutDashboard, FileCode2, Trophy, User,
    LogOut, Menu, X, ChevronDown,
} from 'lucide-react'
import { useAuthStore } from '../../hooks/useAuthStore'
import wsService from '../../services/websocket'

/**
 * Navbar â€” responsive top navigation with user dropdown.
 * Skills: JavaScript, Front-End Web Development, HTML/CSS
 */
function Navbar() {
    const navigate = useNavigate()
    const { pathname } = useLocation()
    const { user, isAuthenticated, logout } = useAuthStore()
    const [menuOpen, setMenuOpen] = useState(false)
    const [dropOpen, setDropOpen] = useState(false)

    const handleLogout = () => {
        wsService.disconnect()
        logout()
        navigate('/login')
        setDropOpen(false)
        setMenuOpen(false)
    }

    const NAV_LINKS = [
        { to: '/dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
        { to: '/reviews', label: 'Reviews', icon: <FileCode2 className="w-4 h-4" /> },
        { to: '/leaderboard', label: 'Leaderboard', icon: <Trophy className="w-4 h-4" /> },
    ]

    const isActive = (to) =>
        to === '/' ? pathname === '/' : pathname.startsWith(to)

    return (
        <nav className="sticky top-0 z-50 bg-slate-950/50 backdrop-blur-xl
                    border-b border-white/10 shadow-lg shadow-black/40">
            <div className="container mx-auto px-4 max-w-7xl">
                <div className="flex items-center justify-between h-16">

                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-2.5 group">
                        <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30
                            flex items-center justify-center
                            group-hover:bg-blue-600/30 transition-colors">
                            <Code2 className="w-4 h-4 text-blue-400" />
                        </div>
                        <span className="font-bold text-slate-100 tracking-tight hidden sm:block">
                            Code<span className="text-blue-400">Review</span>
                        </span>
                    </Link>

                    {/* Desktop nav links */}
                    {isAuthenticated && (
                        <div className="hidden md:flex items-center gap-1">
                            {NAV_LINKS.map(({ to, label, icon }) => (
                                <Link
                                    key={to}
                                    to={to}
                                    className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm
                              font-medium transition-all duration-200 ${isActive(to)
                                            ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30'
                                            : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
                                        }`}
                                >
                                    {icon} {label}
                                </Link>
                            ))}
                        </div>
                    )}

                    {/* Right side */}
                    <div className="flex items-center gap-2">

                        {isAuthenticated ? (
                            <>
                                {/* Points badge */}
                                <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5
                                 bg-amber-500/10 border border-amber-500/20 rounded-full
                                 text-amber-400 text-xs font-medium">
                                    â­ {user?.points ?? 0} pts
                                </div>

                                {/* User dropdown */}
                                <div className="relative">
                                    <button
                                        onClick={() => setDropOpen((o) => !o)}
                                        className="flex items-center gap-2 px-3 py-2 rounded-lg
                               bg-slate-800 hover:bg-slate-700 border border-slate-700
                               transition-colors text-sm text-slate-200"
                                        aria-expanded={dropOpen}
                                        aria-haspopup="true"
                                    >
                                        <div className="w-6 h-6 rounded-full bg-blue-600/30 border border-blue-500/40
                                    flex items-center justify-center text-xs font-bold text-blue-300">
                                            {user?.fullName?.[0] ?? user?.username?.[0] ?? '?'}
                                        </div>
                                        <span className="hidden sm:block max-w-[100px] truncate">
                                            {user?.username}
                                        </span>
                                        <ChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform ${dropOpen ? 'rotate-180' : ''}`} />
                                    </button>

                                    {/* Dropdown menu */}
                                    {dropOpen && (
                                        <>
                                            {/* Backdrop (close on outside click) */}
                                            <div className="fixed inset-0 z-10" onClick={() => setDropOpen(false)} />
                                            <div className="absolute right-0 top-full mt-2 w-52 z-20
                                      bg-slate-900 border border-slate-700 rounded-xl
                                      shadow-xl shadow-black/40 overflow-hidden animate-slide-up">
                                                {/* User info */}
                                                <div className="px-4 py-3 border-b border-slate-800">
                                                    <p className="text-sm font-medium text-slate-200 truncate">
                                                        {user?.fullName ?? user?.username}
                                                    </p>
                                                    <p className="text-xs text-slate-400 truncate">{user?.email}</p>
                                                </div>

                                                <div className="py-1">
                                                    <button
                                                        onClick={() => { navigate('/profile'); setDropOpen(false) }}
                                                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm
                                       text-slate-300 hover:bg-slate-800 hover:text-slate-100
                                       transition-colors"
                                                    >
                                                        <User className="w-4 h-4" /> Profile
                                                    </button>
                                                    <div className="border-t border-slate-800 my-1" />
                                                    <button
                                                        onClick={handleLogout}
                                                        className="flex items-center gap-3 w-full px-4 py-2.5 text-sm
                                       text-red-400 hover:bg-red-500/10 hover:text-red-300
                                       transition-colors"
                                                    >
                                                        <LogOut className="w-4 h-4" /> Sign out
                                                    </button>
                                                </div>
                                            </div>
                                        </>
                                    )}
                                </div>

                                {/* Mobile menu toggle */}
                                <button
                                    onClick={() => setMenuOpen((o) => !o)}
                                    className="md:hidden btn btn-secondary btn-sm"
                                    aria-label="Toggle menu"
                                >
                                    {menuOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
                                </button>
                            </>
                        ) : (
                            <div className="flex items-center gap-2">
                                <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
                                <Link to="/register" className="btn btn-primary btn-sm">Get started</Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Mobile nav */}
            {isAuthenticated && menuOpen && (
                <div className="md:hidden border-t border-slate-800 bg-slate-900 px-4 py-3 space-y-1 animate-slide-up">
                    {NAV_LINKS.map(({ to, label, icon }) => (
                        <Link
                            key={to}
                            to={to}
                            onClick={() => setMenuOpen(false)}
                            className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                          transition-colors ${isActive(to)
                                    ? 'bg-blue-600/20 text-blue-400'
                                    : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800'
                                }`}
                        >
                            {icon} {label}
                        </Link>
                    ))}
                </div>
            )}
        </nav>
    )
}

export default Navbar
