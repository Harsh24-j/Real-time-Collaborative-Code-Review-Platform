import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
    ArrowLeft, Code2, User, Calendar, Sparkles,
    CheckCircle, XCircle, MessageSquare, AlertTriangle,
    Wifi, WifiOff, RefreshCw,
} from 'lucide-react'
import { reviewAPI, commentAPI } from '../../services/api'
import { useWebSocket } from '../../hooks/useWebSocket'
import CodeEditor from './CodeEditor'
import CommentPanel from './CommentPanel'
import AISuggestionsPanel from './AISuggestionsPanel'

/**
 * ReviewDetail — full-screen review page with real-time WebSocket collaboration.
 * Skills: JavaScript, Full-Stack Web Development, WebSocket
 */
function ReviewDetail() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { isConnected, subscribeToReview, subscribeToCursors, unsubscribe, sendComment } =
        useWebSocket()

    const [review, setReview] = useState(null)
    const [comments, setComments] = useState([])
    const [aiSuggestions, setAiSuggestions] = useState([])
    const [cursors, setCursors] = useState([])
    const [loading, setLoading] = useState(true)
    const [selectedLine, setSelectedLine] = useState(null)
    const [activeTab, setActiveTab] = useState('comments') // 'comments' | 'ai'
    const [statusLoading, setStatusLoading] = useState(false)

    // ── Load data ──────────────────────────────────────────────────────────

    const loadReview = useCallback(async () => {
        try {
            setLoading(true)
            const [rev, cmts] = await Promise.all([
                reviewAPI.getById(id),
                reviewAPI.getComments(id),
            ])
            setReview(rev)
            setComments(cmts)
        } catch {
            toast.error('Failed to load review')
            navigate('/reviews')
        } finally {
            setLoading(false)
        }
    }, [id, navigate])

    useEffect(() => { loadReview() }, [loadReview])

    // ── WebSocket subscriptions ────────────────────────────────────────────

    useEffect(() => {
        if (!isConnected || !id) return

        subscribeToReview(id, (incoming) => {
            // Server broadcasts CommentResponse objects directly
            setComments((prev) => {
                const exists = prev.some((c) => c.id === incoming.id)
                return exists ? prev : [...prev, incoming]
            })
        })

        subscribeToCursors(id, (cur) => {
            setCursors((prev) => {
                const without = prev.filter((c) => c.username !== cur.username)
                return [...without, cur]
            })
        })

        return () => unsubscribe(id)
    }, [isConnected, id, subscribeToReview, subscribeToCursors, unsubscribe])

    // ── Handlers ───────────────────────────────────────────────────────────

    const handleAddComment = useCallback(async (commentText, lineNumber) => {
        if (!commentText?.trim()) return
        const payload = { reviewId: Number(id), lineNumber, commentText: commentText.trim() }
        try {
            await commentAPI.create(id, payload)
            sendComment(id, payload) // also broadcast via WS for collaborators
            toast.success('Comment added')
            setSelectedLine(null)
        } catch {
            toast.error('Failed to add comment')
        }
    }, [id, sendComment])

    const handleResolve = useCallback(async (commentId) => {
        try {
            await commentAPI.resolve(commentId)
            setComments((prev) =>
                prev.map((c) => c.id === commentId ? { ...c, isResolved: true } : c))
        } catch {
            toast.error('Failed to resolve comment')
        }
    }, [])

    const handleStatusChange = useCallback(async (newStatus) => {
        setStatusLoading(true)
        try {
            await reviewAPI.updateStatus(id, newStatus)
            setReview((prev) => ({ ...prev, status: newStatus }))
            toast.success(`Status → ${newStatus}`)
        } catch {
            toast.error('Failed to update status')
        } finally {
            setStatusLoading(false)
        }
    }, [id])

    const handleReanalyse = useCallback(async () => {
        try {
            await reviewAPI.triggerAnalysis(id)
            toast.success('AI analysis started — results will appear shortly')
        } catch {
            toast.error('Failed to trigger analysis')
        }
    }, [id])

    // ── Status helpers ─────────────────────────────────────────────────────

    const STATUS_BADGE = {
        OPEN: 'badge badge-blue',
        IN_REVIEW: 'badge badge-yellow',
        APPROVED: 'badge badge-green',
        REJECTED: 'badge badge-red',
        CLOSED: 'badge badge-gray',
    }

    // ── Loading / empty states ─────────────────────────────────────────────

    if (loading) {
        return (
            <div className="flex items-center justify-center h-[80vh] gap-3 text-slate-400">
                <span className="spinner" /> Loading review…
            </div>
        )
    }
    if (!review) return null

    const aiSuggestionsFromReview = aiSuggestions  // from separate API call if needed
    const unresolvedCount = comments.filter((c) => !c.isResolved).length

    return (
        <div className="h-[calc(100vh-80px)] flex flex-col -mx-4 -mt-8">

            {/* ── Header ─────────────────────────────────────────────────────── */}
            <header className="flex items-start justify-between gap-4
                         bg-slate-900 border-b border-slate-800 px-6 py-4 flex-shrink-0">
                <div className="flex items-start gap-4 min-w-0">
                    <button onClick={() => navigate('/reviews')} className="btn btn-secondary btn-sm mt-1">
                        <ArrowLeft className="w-4 h-4" />
                    </button>
                    <div className="min-w-0">
                        <div className="flex items-center gap-3 flex-wrap">
                            <h1 className="page-title truncate">{review.title}</h1>
                            <span className={STATUS_BADGE[review.status] ?? 'badge badge-gray'}>
                                {review.status}
                            </span>
                            {isConnected
                                ? <span className="flex items-center gap-1 text-xs text-emerald-400">
                                    <Wifi className="w-3 h-3" /> Live
                                </span>
                                : <span className="flex items-center gap-1 text-xs text-slate-500">
                                    <WifiOff className="w-3 h-3" /> Offline
                                </span>}
                        </div>

                        <div className="flex flex-wrap items-center gap-4 mt-1.5 text-xs text-slate-400">
                            <span className="flex items-center gap-1">
                                <User className="w-3.5 h-3.5" />
                                {review.creatorUsername}
                            </span>
                            <span className="flex items-center gap-1">
                                <Code2 className="w-3.5 h-3.5" />
                                {review.language}
                            </span>
                            <span className="flex items-center gap-1">
                                <Calendar className="w-3.5 h-3.5" />
                                {new Date(review.createdAt).toLocaleDateString()}
                            </span>
                            {review.qualityScore != null && (
                                <span className="flex items-center gap-1 text-amber-400 font-medium">
                                    <Sparkles className="w-3.5 h-3.5" />
                                    {review.qualityScore.toFixed(1)} / 10
                                </span>
                            )}
                        </div>

                        {review.description && (
                            <p className="mt-2 text-sm text-slate-400 max-w-2xl line-clamp-2">
                                {review.description}
                            </p>
                        )}
                    </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2 flex-shrink-0">
                    <button onClick={handleReanalyse} className="btn btn-secondary btn-sm">
                        <RefreshCw className="w-3.5 h-3.5" /> Re-analyse
                    </button>
                    {review.status !== 'APPROVED' && (
                        <button
                            onClick={() => handleStatusChange('APPROVED')}
                            disabled={statusLoading}
                            className="btn btn-sm bg-emerald-600 hover:bg-emerald-500 text-white shadow-lg shadow-emerald-600/20"
                        >
                            <CheckCircle className="w-3.5 h-3.5" /> Approve
                        </button>
                    )}
                    {review.status !== 'REJECTED' && (
                        <button
                            onClick={() => handleStatusChange('REJECTED')}
                            disabled={statusLoading}
                            className="btn btn-sm bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-600/20"
                        >
                            <XCircle className="w-3.5 h-3.5" /> Reject
                        </button>
                    )}
                </div>
            </header>

            {/* ── Main Content ────────────────────────────────────────────────── */}
            <div className="flex flex-1 overflow-hidden">

                {/* Code Editor */}
                <div className="flex-1 bg-slate-950 overflow-hidden">
                    <CodeEditor
                        code={review.codeContent}
                        language={review.language}
                        readOnly
                        comments={comments}
                        aiSuggestions={aiSuggestionsFromReview}
                        collaboratorCursors={cursors}
                        onLineClick={setSelectedLine}
                    />
                </div>

                {/* Right panel */}
                <aside className="w-96 flex flex-col bg-slate-900 border-l border-slate-800 flex-shrink-0">

                    {/* Tabs */}
                    <div className="flex border-b border-slate-800 flex-shrink-0">
                        <button
                            onClick={() => setActiveTab('comments')}
                            className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium
                          border-b-2 transition-colors ${activeTab === 'comments'
                                    ? 'border-blue-500 text-blue-400'
                                    : 'border-transparent text-slate-400 hover:text-slate-200'}`}
                        >
                            <MessageSquare className="w-4 h-4" />
                            Comments
                            {unresolvedCount > 0 && (
                                <span className="ml-0.5 badge badge-red text-[10px]">{unresolvedCount}</span>
                            )}
                        </button>
                        <button
                            onClick={() => setActiveTab('ai')}
                            className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium
                          border-b-2 transition-colors ${activeTab === 'ai'
                                    ? 'border-amber-500 text-amber-400'
                                    : 'border-transparent text-slate-400 hover:text-slate-200'}`}
                        >
                            <Sparkles className="w-4 h-4" />
                            AI
                            {review.criticalIssues > 0 && (
                                <span className="ml-0.5 badge badge-red text-[10px]">
                                    <AlertTriangle className="w-2.5 h-2.5" />
                                </span>
                            )}
                        </button>
                    </div>

                    {/* Panel content */}
                    <div className="flex-1 overflow-y-auto">
                        {activeTab === 'comments' ? (
                            <CommentPanel
                                comments={comments}
                                selectedLine={selectedLine}
                                onAddComment={handleAddComment}
                                onResolve={handleResolve}
                                onClearLine={() => setSelectedLine(null)}
                            />
                        ) : (
                            <AISuggestionsPanel
                                reviewId={Number(id)}
                                critical={review.criticalIssues}
                                warnings={review.warningIssues}
                                info={review.infoIssues}
                            />
                        )}
                    </div>
                </aside>
            </div>
        </div>
    )
}

export default ReviewDetail
