import { useState, useEffect, useCallback } from 'react'
import wsService from '../services/websocket'

/**
 * useWebSocket â€” React hook wrapping the singleton WebSocketService.
 * Connects on mount and disconnects on unmount.
 * Skills: JavaScript, Front-End Web Development, Full-Stack Web Development
 */
export function useWebSocket() {
    const [isConnected, setIsConnected] = useState(wsService.isConnected)

    useEffect(() => {
        if (!wsService.isConnected) {
            wsService.connect(
                () => setIsConnected(true),
                (err) => { console.error('[useWebSocket] error:', err); setIsConnected(false) }
            )
        } else {
            setIsConnected(true)
        }
        return () => {
            // Don't disconnect on every unmount â€” the singleton persists across pages.
            // Call wsService.disconnect() only on explicit logout.
        }
    }, [])

    const subscribeToReview = useCallback((id, cb) => wsService.subscribeToReview(id, cb), [])
    const subscribeToCursors = useCallback((id, cb) => wsService.subscribeToCursors(id, cb), [])
    const subscribeToTyping = useCallback((id, cb) => wsService.subscribeToTyping(id, cb), [])
    const unsubscribe = useCallback((id) => wsService.unsubscribeFromReview(id), [])
    const sendComment = useCallback((id, data) => wsService.sendComment(id, data), [])
    const sendCursor = useCallback((id, pos) => wsService.sendCursor(id, pos), [])
    const sendTyping = useCallback((id, t) => wsService.sendTyping(id, t), [])

    return {
        isConnected,
        subscribeToReview,
        subscribeToCursors,
        subscribeToTyping,
        unsubscribe,
        sendComment,
        sendCursor,
        sendTyping,
    }
}
