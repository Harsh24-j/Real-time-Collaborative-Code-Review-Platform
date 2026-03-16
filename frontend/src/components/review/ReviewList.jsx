import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
    Plus, Search, Code2, MessageSquare, Sparkles,
    Clock, CheckCircle, XCircle, RefreshCw, ChevronRight,
} from 'lucide-react'
import { reviewAPI } from '../../services/api'
import BackgroundWrapper from '../common/BackgroundWrapper'

const STATUS_BADGE = {
    OPEN: 'badge badge-blue',
    IN_REVIEW: 'badge badge-yellow',
    APPROVED: 'badge badge-green',
    REJECTED: 'badge badge-red',
    CLOSED: 'badge badge-gray',
}

const STATUS_OPTIONS = ['ALL', 'OPEN', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'CLOSED']

const LANGUAGE_COLOURS = {
    java: 'text-amber-400',
    python: 'text-blue-400',
    javascript: 'text-yellow-400',
    typescript: 'text-blue-500',
    go: 'text-cyan-400',
    rust: 'text-orange-400',
}

/**
 * ReviewList — paginated/searchable/filterable review grid.
 * Skills: JavaScript, Front-End Web Development, Responsive Web Design
 */
function ReviewList() {
    const navigate = useNavigate()
    const [reviews, setReviews] = useState([])
    const [loading, setLoading] = useState(true)
    const [searchTerm, setSearchTerm] = useState('')
    const [filterStatus, setFilterStatus] = useState('ALL')

    const loadReviews = async () => {
        try {
            setLoading(true)
            const data = await reviewAPI.getAll()
            // API returns Page<ReviewResponse> or a plain list
            setReviews(Array.isArray(data) ? data : data.content ?? [])
        } catch {
            toast.error('Failed to load reviews')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { loadReviews() }, [])

    // Client-side filter (server search called separately)
    const filtered = reviews.filter((r) => {
        const matchSearch = !searchTerm ||
            r.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (r.description ?? '').toLowerCase().includes(searchTerm.toLowerCase())
        const matchStatus = filterStatus === 'ALL' || r.status === filterStatus
        return matchSearch && matchStatus
    })

    const handleServerSearch = async () => {
        if (!searchTerm.trim()) { loadReviews(); return }
        try {
            setLoading(true)
            const data = await reviewAPI.search(searchTerm)
            setReviews(Array.isArray(data) ? data : data.content ?? [])
        } catch {
            toast.error('Search failed')
        } finally {
            setLoading(false)
        }
    }

    return (
        <BackgroundWrapper variant="code2">
            <div className="animate-fade-in relative z-10 p-4">

            {/* ── Header ─────────────────────────────────────────────────────── */}
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="page-title">Code Reviews</h1>
                    <p className="page-subtitle">
                        {reviews.length} review{reviews.length !== 1 ? 's' : ''} found
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <button onClick={loadReviews} className="btn btn-secondary btn-sm">
                        <RefreshCw className="w-4 h-4" />
                    </button>
                    <button onClick={() => navigate('/reviews/create')} className="btn btn-primary">
                        <Plus className="w-4 h-4" /> New Review
                    </button>
                </div>
            </div>

            {/* ── Filters ────────────────────────────────────────────────────── */}
            <div className="glass-card-dark p-4 mb-6 flex gap-3 shadow-lg">
                <div className="flex-1 relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500 pointer-events-none" />
                    <input
                        type="text"
                        placeholder="Search reviews…"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleServerSearch()}
                        className="input pl-9"
                    />
                </div>
                <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="input w-44"
                >
                    {STATUS_OPTIONS.map((s) => (
                        <option key={s} value={s}>{s === 'ALL' ? 'All Status' : s.replace('_', ' ')}</option>
                    ))}
                </select>
            </div>

            {/* ── Content ────────────────────────────────────────────────────── */}
            {loading ? (
                <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="glass-card-dark p-6 animate-pulse space-y-3">
                            <div className="skeleton h-5 w-3/4 rounded bg-white/10" />
                            <div className="skeleton h-4 w-full rounded" />
                            <div className="skeleton h-4 w-2/3 rounded" />
                            <div className="skeleton h-3 w-1/2 rounded mt-4" />
                        </div>
                    ))}
                </div>
            ) : filtered.length === 0 ? (
                <div className="glass-card-dark p-12 text-center py-16 shadow-xl">
                    <Code2 className="w-14 h-14 mx-auto text-indigo-400 mb-4 opacity-50" />
                    <h3 className="text-lg font-semibold text-slate-200 mb-1">No reviews found</h3>
                    <p className="text-slate-400 text-sm mb-6">
                        {searchTerm ? 'Try a different search term' : 'Be the first to submit a code review'}
                    </p>
                    <button onClick={() => navigate('/reviews/create')} className="btn btn-primary mx-auto">
                        <Plus className="w-4 h-4" /> Create Review
                    </button>
                </div>
            ) : (
                <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {filtered.map((review) => (
                        <ReviewCard key={review.id} review={review} />
                    ))}
                </div>
            )}
            </div>
        </BackgroundWrapper>
    )
}

// ── ReviewCard ─────────────────────────────────────────────────────────────

function ReviewCard({ review }) {
    const navigate = useNavigate()
    const langColour = LANGUAGE_COLOURS[review.language?.toLowerCase()] ?? 'text-slate-400'

    return (
        <div
            onClick={() => navigate(`/reviews/${review.id}`)}
            className="glass-card-dark p-6 cursor-pointer hover:border-blue-500/50 hover:shadow-2xl hover:shadow-blue-500/10 transition-all duration-300 group animate-fade-in"
        >
            {/* Title + status */}
            <div className="flex items-start justify-between gap-3 mb-3">
                <h3 className="font-semibold text-slate-100 leading-snug line-clamp-2 flex-1 group-hover:text-blue-300 transition-colors">
                    {review.title}
                </h3>
                <span className={`${STATUS_BADGE[review.status] ?? 'badge badge-gray'} flex-shrink-0`}>
                    {review.status}
                </span>
            </div>

            {/* Description */}
            <p className="text-slate-400 text-sm line-clamp-2 mb-4 min-h-[2.5rem]">
                {review.description || 'No description provided'}
            </p>

            {/* Stats row */}
            <div className="flex items-center gap-4 text-xs text-slate-500">
                <span className={`flex items-center gap-1 font-mono font-medium ${langColour}`}>
                    <Code2 className="w-3.5 h-3.5" />
                    {review.language}
                </span>
                <span className="flex items-center gap-1">
                    <MessageSquare className="w-3.5 h-3.5" />
                    {review.commentCount ?? 0}
                </span>
                {review.qualityScore != null && (
                    <span className="flex items-center gap-1 text-amber-400">
                        <Sparkles className="w-3.5 h-3.5" />
                        {review.qualityScore.toFixed(1)}
                    </span>
                )}
                {review.criticalIssues > 0 && (
                    <span className="flex items-center gap-1 text-red-400">
                        <XCircle className="w-3.5 h-3.5" />
                        {review.criticalIssues}
                    </span>
                )}
            </div>

            {/* Footer */}
            <div className="divider mb-3" />
            <div className="flex items-center justify-between text-xs text-slate-500">
                <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    {new Date(review.createdAt).toLocaleDateString()}
                </span>
                <span className="text-slate-500">by <strong className="text-slate-400">{review.creatorUsername}</strong></span>
                <ChevronRight className="w-4 h-4 group-hover:text-blue-400 group-hover:translate-x-0.5 transition-all" />
            </div>
        </div>
    )
}

export default ReviewList
