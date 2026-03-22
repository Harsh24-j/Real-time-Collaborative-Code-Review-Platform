import { useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { commentAPI } from '../services/api'

/**
 * useComments â€” comment CRUD with optimistic updates.
 * Skills: JavaScript, Front-End Web Development
 */
export function useComments(reviewId) {
    const [comments, setComments] = useState([])
    const [loading, setLoading] = useState(false)

    const fetchComments = useCallback(async () => {
        setLoading(true)
        try {
            const data = await commentAPI.unresolved(reviewId)   // or use reviewAPI.getComments
            setComments(Array.isArray(data) ? data : [])
        } catch {
            toast.error('Failed to load comments')
        } finally {
            setLoading(false)
        }
    }, [reviewId])

    /** Optimistic add â€” appends a placeholder until the server echo arrives. */
    const addComment = useCallback(async (commentText, lineNumber = null) => {
        const payload = { reviewId: Number(reviewId), commentText: commentText.trim(), lineNumber }
        try {
            const created = await commentAPI.create(reviewId, payload)
            setComments((prev) => {
                const exists = prev.some((c) => c.id === created.id)
                return exists ? prev : [...prev, created]
            })
            return created
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to add comment')
            return null
        }
    }, [reviewId])

    /** Called when WebSocket broadcasts a new comment â€” avoids duplicates. */
    const receiveComment = useCallback((comment) => {
        setComments((prev) => {
            const exists = prev.some((c) => c.id === comment.id)
            return exists ? prev : [...prev, comment]
        })
    }, [])

    const updateComment = useCallback(async (id, commentText) => {
        try {
            const updated = await commentAPI.update(id, commentText)
            setComments((prev) => prev.map((c) => c.id === id ? updated : c))
            return updated
        } catch {
            toast.error('Failed to update comment')
            return null
        }
    }, [])

    const deleteComment = useCallback(async (id) => {
        // Optimistic removal
        setComments((prev) => prev.filter((c) => c.id !== id))
        try {
            await commentAPI.delete(id)
        } catch {
            toast.error('Failed to delete comment')
            fetchComments()  // revert
        }
    }, [fetchComments])

    const resolveComment = useCallback(async (id) => {
        try {
            const updated = await commentAPI.resolve(id)
            setComments((prev) => prev.map((c) => c.id === id ? { ...c, isResolved: true } : c))
            return updated
        } catch {
            toast.error('Failed to resolve comment')
            return null
        }
    }, [])

    const unresolveComment = useCallback(async (id) => {
        try {
            const updated = await commentAPI.unresolve(id)
            setComments((prev) => prev.map((c) => c.id === id ? { ...c, isResolved: false } : c))
            return updated
        } catch {
            toast.error('Failed to unresolve comment')
            return null
        }
    }, [])

    return {
        comments, loading,
        fetchComments, addComment, receiveComment,
        updateComment, deleteComment, resolveComment, unresolveComment,
    }
}
