import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    Code2, MessageSquare, Award, TrendingUp, Plus, ArrowRight,
    CheckCircle, XCircle, Clock, Sparkles, AlertTriangle, Users,
} from 'lucide-react'
import { analyticsAPI, reviewAPI } from '../../services/api'
import { useAuthStore } from '../../hooks/useAuthStore'

/**
 * Dashboard — platform overview with stats, recent reviews, and quick actions.
 * Skills: JavaScript, Front-End Web Development, Full-Stack Web Development
 */
function Dashboard() {
    const navigate = useNavigate()
    const { user } = useAuthStore()
    const [stats, setStats] = useState(null)
    const [reviews, setReviews] = useState([])
    const [trends, setTrends] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        (async () => {
            try {
                const [s, r, t] = await Promise.all([
                    analyticsAPI.getDashboard(),
                    reviewAPI.getAll({ page: 0, size: 5, sortBy: 'createdAt' }),
                    analyticsAPI.getTrends(7),
                ])
                setStats(s)
                setReviews(Array.isArray(r) ? r.slice(0, 5) : (r.content ?? []).slice(0, 5))
                setTrends(t)
            } catch {
                /* silent — individual cards handle null gracefully */
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    // ── Status helpers ─────────────────────────────────────────────────────

    const STATUS_ICON = {
        OPEN: <Clock className="w-3.5 h-3.5 text-blue-400" />,
        IN_REVIEW: <Clock className="w-3.5 h-3.5 text-amber-400" />,
        APPROVED: <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />,
        REJECTED: <XCircle className="w-3.5 h-3.5 text-red-400" />,
        CLOSED: <CheckCircle className="w-3.5 h-3.5 text-slate-400" />,
    }

    const STATUS_BADGE = {
        OPEN: 'badge badge-blue',
        IN_REVIEW: 'badge badge-yellow',
        APPROVED: 'badge badge-green',
        REJECTED: 'badge badge-red',
        CLOSED: 'badge badge-gray',
    }

    // ── Stat card ──────────────────────────────────────────────────────────

    const StatCard = ({ icon, label, value, sub, iconBg }) => (
        <div className="card flex items-start gap-4">
            <div className={`flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center ${iconBg}`}>
                {icon}
            </div>
            <div>
                <p className="text-xs text-slate-400 uppercase tracking-wider">{label}</p>
                <p className="text-2xl font-bold text-slate-100 mt-0.5">
                    {loading ? <span className="skeleton inline-block w-12 h-6 rounded" /> : (value ?? '—')}
                </p>
                {sub && <p className="text-xs text-slate-500 mt-1">{sub}</p>}
            </div>
        </div>
    )

    // ── Language distribution ──────────────────────────────────────────────

    const topLangs = trends?.languageDistribution
        ? Object.entries(trends.languageDistribution)
            .sort(([, a], [, b]) => b - a)
            .slice(0, 5)
        : []

    return (
        <div className="space-y-8 animate-fade-in">

            {/* Welcome */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="page-title">
                        Welcome back, {user?.fullName?.split(' ')[0] ?? user?.username} 👋
                    </h1>
                    <p className="page-subtitle">Here's what's happening on the platform today</p>
                </div>
                <button onClick={() => navigate('/reviews/create')} className="btn btn-primary">
                    <Plus className="w-4 h-4" /> New Review
                </button>
            </div>

            {/* Stats grid */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard
                    icon={<Code2 className="w-6 h-6 text-blue-400" />}
                    iconBg="bg-blue-500/15"
                    label="Total Reviews"
                    value={stats?.totalReviews}
                    sub={`${stats?.openReviews ?? 0} open`}
                />
                <StatCard
                    icon={<MessageSquare className="w-6 h-6 text-emerald-400" />}
                    iconBg="bg-emerald-500/15"
                    label="Total Comments"
                    value={stats?.totalComments}
                    sub={`${stats?.reviewsLast24h ?? 0} reviews today`}
                />
                <StatCard
                    icon={<Award className="w-6 h-6 text-amber-400" />}
                    iconBg="bg-amber-500/15"
                    label="Your Points"
                    value={user?.points ?? 0}
                    sub="Gamification score"
                />
                <StatCard
                    icon={<AlertTriangle className="w-6 h-6 text-red-400" />}
                    iconBg="bg-red-500/15"
                    label="Critical Issues"
                    value={stats?.criticalIssues}
                    sub="Needs attention"
                />
            </div>

            {/* Main two-column layout */}
            <div className="grid lg:grid-cols-3 gap-6">

                {/* Recent Reviews (2/3 width) */}
                <div className="lg:col-span-2 card space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-base font-semibold text-slate-200">Recent Reviews</h2>
                        <button
                            onClick={() => navigate('/reviews')}
                            className="btn btn-ghost btn-sm"
                        >
                            View all <ArrowRight className="w-3.5 h-3.5" />
                        </button>
                    </div>

                    {loading ? (
                        <div className="space-y-3">
                            {Array.from({ length: 4 }).map((_, i) => (
                                <div key={i} className="skeleton h-14 rounded-lg" />
                            ))}
                        </div>
                    ) : reviews.length === 0 ? (
                        <div className="text-center py-8 text-slate-500 text-sm">
                            <Code2 className="w-10 h-10 mx-auto mb-2 opacity-30" />
                            No reviews yet —{' '}
                            <button onClick={() => navigate('/reviews/create')}
                                className="text-blue-400 hover:underline">create one</button>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            {reviews.map((r) => (
                                <div
                                    key={r.id}
                                    onClick={() => navigate(`/reviews/${r.id}`)}
                                    className="flex items-center justify-between p-3 rounded-lg
                             bg-slate-800/50 hover:bg-slate-800 border border-transparent
                             hover:border-slate-700 cursor-pointer transition-all group"
                                >
                                    <div className="flex items-center gap-3 min-w-0">
                                        <span>{STATUS_ICON[r.status]}</span>
                                        <div className="min-w-0">
                                            <p className="text-sm font-medium text-slate-200 truncate
                                   group-hover:text-blue-300 transition-colors">
                                                {r.title}
                                            </p>
                                            <p className="text-xs text-slate-500">
                                                {r.language} · by {r.creatorUsername}
                                            </p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 flex-shrink-0">
                                        {r.qualityScore != null && (
                                            <span className="text-xs text-amber-400 font-medium flex items-center gap-1">
                                                <Sparkles className="w-3 h-3" />
                                                {r.qualityScore.toFixed(1)}
                                            </span>
                                        )}
                                        <span className={STATUS_BADGE[r.status] ?? 'badge badge-gray'}>
                                            {r.status}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Right sidebar (1/3 width) */}
                <div className="space-y-6">

                    {/* Status breakdown */}
                    <div className="card space-y-3">
                        <h2 className="text-base font-semibold text-slate-200">Status Overview</h2>
                        {[
                            { label: 'Open', key: 'openReviews', cls: 'bg-blue-500' },
                            { label: 'In Review', key: 'inReview', cls: 'bg-amber-500' },
                            { label: 'Approved', key: 'approvedReviews', cls: 'bg-emerald-500' },
                            { label: 'Rejected', key: 'rejectedReviews', cls: 'bg-red-500' },
                            { label: 'Closed', key: 'closedReviews', cls: 'bg-slate-500' },
                        ].map(({ label, key, cls }) => {
                            const count = stats?.[key] ?? 0
                            const total = stats?.totalReviews || 1
                            const pct = Math.round((count / total) * 100)
                            return (
                                <div key={key}>
                                    <div className="flex justify-between text-xs mb-1">
                                        <span className="text-slate-400">{label}</span>
                                        <span className="text-slate-300 font-medium">{count}</span>
                                    </div>
                                    <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
                                        <div className={`h-full ${cls} rounded-full transition-all duration-700`}
                                            style={{ width: `${pct}%` }} />
                                    </div>
                                </div>
                            )
                        })}
                    </div>

                    {/* Language distribution */}
                    {topLangs.length > 0 && (
                        <div className="card space-y-3">
                            <h2 className="text-base font-semibold text-slate-200 flex items-center gap-2">
                                <TrendingUp className="w-4 h-4 text-blue-400" /> Top Languages
                            </h2>
                            {topLangs.map(([lang, count]) => (
                                <div key={lang} className="flex justify-between text-sm">
                                    <span className="text-slate-400 font-mono">{lang}</span>
                                    <span className="text-slate-300 font-medium">{count}</span>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Quick actions */}
                    <div className="card space-y-2">
                        <h2 className="text-base font-semibold text-slate-200">Quick Actions</h2>
                        {[
                            { label: 'New Review', path: '/reviews/create', icon: <Plus className="w-4 h-4" />, cls: 'btn-primary' },
                            { label: 'Leaderboard', path: '/leaderboard', icon: <Award className="w-4 h-4" />, cls: 'btn-secondary' },
                            { label: 'My Profile', path: '/profile', icon: <Users className="w-4 h-4" />, cls: 'btn-secondary' },
                        ].map(({ label, path, icon, cls }) => (
                            <button key={path} onClick={() => navigate(path)}
                                className={`btn ${cls} w-full justify-start`}>
                                {icon} {label}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Dashboard
