import { useState, useCallback } from 'react'
import toast from 'react-hot-toast'
import { reviewAPI } from '../services/api'

/**
 * useReview â€” review CRUD and status management.
 * Skills: JavaScript, Front-End Web Development
 */
export function useReview() {
    const [reviews, setReviews] = useState([])
    const [review, setReview] = useState(null)
    const [loading, setLoading] = useState(false)
    const [total, setTotal] = useState(0)

    const fetchAll = useCallback(async (params = {}) => {
        setLoading(true)
        try {
            const data = await reviewAPI.getAll(params)
            const list = Array.isArray(data) ? data : (data.content ?? [])
            setReviews(list)
            setTotal(Array.isArray(data) ? data.length : (data.totalElements ?? list.length))
        } catch {
            toast.error('Failed to load reviews')
        } finally {
            setLoading(false)
        }
    }, [])

    const fetchById = useCallback(async (id) => {
        setLoading(true)
        try {
            const data = await reviewAPI.getById(id)
            setReview(data)
            return data
        } catch {
            toast.error('Failed to load review')
            return null
        } finally {
            setLoading(false)
        }
    }, [])

    const createReview = useCallback(async (payload) => {
        setLoading(true)
        try {
            const created = await reviewAPI.create(payload)
            setReviews((prev) => [created, ...prev])
            toast.success('Review created â€” AI analysis started!')
            return created
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to create review')
            return null
        } finally {
            setLoading(false)
        }
    }, [])

    const updateReview = useCallback(async (id, payload) => {
        try {
            const updated = await reviewAPI.update(id, payload)
            setReviews((prev) => prev.map((r) => r.id === id ? updated : r))
            if (review?.id === id) setReview(updated)
            return updated
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to update review')
            return null
        }
    }, [review])

    const deleteReview = useCallback(async (id) => {
        try {
            await reviewAPI.delete(id)
            setReviews((prev) => prev.filter((r) => r.id !== id))
            toast.success('Review deleted')
        } catch {
            toast.error('Failed to delete review')
        }
    }, [])

    const updateStatus = useCallback(async (id, status) => {
        try {
            const updated = await reviewAPI.updateStatus(id, status)
            setReviews((prev) => prev.map((r) => r.id === id ? { ...r, status } : r))
            if (review?.id === id) setReview((prev) => ({ ...prev, status }))
            return updated
        } catch {
            toast.error('Failed to update status')
            return null
        }
    }, [review])

    const search = useCallback(async (term) => {
        setLoading(true)
        try {
            const data = await reviewAPI.search(term)
            setReviews(Array.isArray(data) ? data : (data.content ?? []))
        } catch {
            toast.error('Search failed')
        } finally {
            setLoading(false)
        }
    }, [])

    return {
        reviews, review, loading, total,
        fetchAll, fetchById, createReview, updateReview, deleteReview, updateStatus, search,
    }
}
