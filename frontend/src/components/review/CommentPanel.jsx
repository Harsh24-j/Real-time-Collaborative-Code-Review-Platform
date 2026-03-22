import React, { useState } from 'react'
import { MessageSquare, CheckCircle, Bot, Send, X } from 'lucide-react'

/**
 * CommentPanel â€” displays comment thread and inline comment input.
 * Skills: JavaScript, Front-End Web Development
 */
function CommentPanel({ comments = [], selectedLine, onAddComment, onResolve, onClearLine }) {
    const [text, setText] = useState('')
    const [submitting, setSubmitting] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!text.trim()) return
        setSubmitting(true)
        await onAddComment(text, selectedLine)
        setText('')
        setSubmitting(false)
    }

    const unresolved = comments.filter((c) => !c.isResolved)
    const resolved = comments.filter((c) => c.isResolved)

    return (
        <div className="flex flex-col h-full">

            {/* Add comment form (shown when line is selected) */}
            {selectedLine != null && (
                <div className="p-4 border-b border-slate-800 bg-slate-800/50 animate-slide-up">
                    <div className="flex items-center justify-between mb-3">
                        <span className="text-xs font-medium text-blue-400">
                            Commenting on line {selectedLine}
                        </span>
                        <button
                            onClick={() => { onClearLine(); setText('') }}
                            className="text-slate-500 hover:text-slate-300 transition-colors"
                            aria-label="Cancel"
                        >
                            <X className="w-4 h-4" />
                        </button>
                    </div>
                    <form onSubmit={handleSubmit} className="space-y-2">
                        <textarea
                            id="comment-input"
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                            className="input text-sm resize-none"
                            rows={3}
                            placeholder="Add your commentâ€¦"
                            autoFocus
                            maxLength={5000}
                        />
                        <button
                            type="submit"
                            disabled={submitting || !text.trim()}
                            className="btn btn-primary btn-sm w-full"
                        >
                            {submitting
                                ? <><span className="spinner" /> Postingâ€¦</>
                                : <><Send className="w-3.5 h-3.5" /> Post comment</>}
                        </button>
                    </form>
                </div>
            )}

            {/* General comment (no line) */}
            {selectedLine == null && (
                <div className="p-4 border-b border-slate-800">
                    <form onSubmit={handleSubmit} className="flex gap-2">
                        <input
                            id="general-comment-input"
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                            className="input text-sm flex-1"
                            placeholder="Add a general commentâ€¦"
                            maxLength={5000}
                        />
                        <button
                            type="submit"
                            disabled={submitting || !text.trim()}
                            className="btn btn-primary btn-sm flex-shrink-0"
                            aria-label="Post"
                        >
                            <Send className="w-3.5 h-3.5" />
                        </button>
                    </form>
                </div>
            )}

            {/* Comment list */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {comments.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-32 text-slate-500 text-sm">
                        <MessageSquare className="w-8 h-8 mb-2 opacity-50" />
                        No comments yet
                    </div>
                ) : (
                    <>
                        {/* Unresolved */}
                        {unresolved.map((c) => (
                            <CommentCard key={c.id} comment={c} onResolve={onResolve} />
                        ))}

                        {/* Resolved section */}
                        {resolved.length > 0 && (
                            <details className="mt-4">
                                <summary className="text-xs text-slate-500 cursor-pointer
                                    hover:text-slate-300 select-none mb-2">
                                    {resolved.length} resolved comment{resolved.length > 1 ? 's' : ''}
                                </summary>
                                <div className="space-y-3 opacity-60">
                                    {resolved.map((c) => (
                                        <CommentCard key={c.id} comment={c} resolved />
                                    ))}
                                </div>
                            </details>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

// â”€â”€ CommentCard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

function CommentCard({ comment, onResolve, resolved = false }) {
    return (
        <div className={`card-sm animate-fade-in ${resolved ? 'opacity-60' : ''}`}>
            {/* Header */}
            <div className="flex items-center justify-between mb-1.5">
                <div className="flex items-center gap-2">
                    {comment.isAiGenerated ? (
                        <span className="flex items-center gap-1 text-amber-400 text-xs font-medium">
                            <Bot className="w-3.5 h-3.5" /> AI
                        </span>
                    ) : (
                        <span className="text-xs font-medium text-slate-300">{comment.username}</span>
                    )}
                    {comment.lineNumber && (
                        <span className="text-xs text-slate-500 font-mono">L{comment.lineNumber}</span>
                    )}
                </div>
                {!resolved && onResolve && !comment.isAiGenerated && (
                    <button
                        onClick={() => onResolve(comment.id)}
                        className="text-slate-500 hover:text-emerald-400 transition-colors"
                        title="Mark resolved"
                    >
                        <CheckCircle className="w-4 h-4" />
                    </button>
                )}
                {resolved && (
                    <CheckCircle className="w-4 h-4 text-emerald-500" />
                )}
            </div>

            {/* Body */}
            <p className="text-sm text-slate-300 leading-relaxed whitespace-pre-wrap break-words">
                {comment.commentText}
            </p>

            {/* Time */}
            <p className="text-[10px] text-slate-600 mt-2">
                {comment.createdAt
                    ? new Date(comment.createdAt).toLocaleString()
                    : 'Just now'}
            </p>
        </div>
    )
}

export default CommentPanel
