import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * WebSocketService — singleton STOMP client over SockJS.
 * Skills: JavaScript, Full-Stack Web Development
 *
 * Usage:
 *   import wsService from './websocket'
 *
 *   // Connect (call once per session)
 *   wsService.connect()
 *
 *   // Subscribe to a review
 *   wsService.subscribeToReview(id, (comment) => setComments(c => [...c, comment]))
 *   wsService.subscribeToCursors(id, (cursor) => updateCursor(cursor))
 *
 *   // Send events
 *   wsService.sendComment(id, { reviewId: id, commentText: '...' })
 *   wsService.sendCursor(id, { line: 12, column: 5 })
 *
 *   // Cleanup
 *   wsService.unsubscribeFromReview(id)
 *   wsService.disconnect()
 */

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws-review'

class WebSocketService {
    constructor() {
        this.client = null
        this.subscriptions = new Map()  // key → STOMP subscription
        this._onConnected = null
        this._onError = null
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    connect(onConnected = null, onError = null) {
        if (this.client?.active) return       // already connected

        this._onConnected = onConnected
        this._onError = onError

        this.client = new Client({
            webSocketFactory: () => new SockJS(WS_URL),
            reconnectDelay: 5_000,
            heartbeatIncoming: 4_000,
            heartbeatOutgoing: 4_000,

            // Attach JWT to STOMP CONNECT frame
            connectHeaders: {
                Authorization: `Bearer ${localStorage.getItem('token') ?? ''}`,
            },

            onConnect: () => {
                console.log('[WS] Connected')
                this._onConnected?.()
            },
            onStompError: (frame) => {
                console.error('[WS] STOMP error:', frame.headers?.message)
                this._onError?.(frame)
            },
            onWebSocketClose: () => {
                console.log('[WS] Connection closed')
            },
            onDisconnect: () => {
                console.log('[WS] Disconnected')
            },
        })

        this.client.activate()
    }

    disconnect() {
        this.subscriptions.forEach((sub) => { try { sub.unsubscribe() } catch { } })
        this.subscriptions.clear()
        this.client?.deactivate()
        this.client = null
    }

    get isConnected() {
        return !!(this.client?.connected)
    }

    // ── Subscriptions ─────────────────────────────────────────────────────

    /** Subscribe to new comments broadcast on a review. */
    subscribeToReview(reviewId, onComment) {
        return this._subscribe(
            `review-${reviewId}-comments`,
            `/topic/review/${reviewId}/comments`,
            onComment
        )
    }

    /** Subscribe to live cursor positions of collaborators. */
    subscribeToCursors(reviewId, onCursor) {
        return this._subscribe(
            `review-${reviewId}-cursors`,
            `/topic/review/${reviewId}/cursors`,
            onCursor
        )
    }

    /** Subscribe to typing-indicator events. */
    subscribeToTyping(reviewId, onTyping) {
        return this._subscribe(
            `review-${reviewId}-typing`,
            `/topic/review/${reviewId}/typing`,
            onTyping
        )
    }

    /** Subscribe to presence events (join/leave). */
    subscribeToPresence(reviewId, onPresence) {
        return this._subscribe(
            `review-${reviewId}-presence`,
            `/topic/review/${reviewId}/presence`,
            onPresence
        )
    }

    /** Unsubscribe from all topics for a review. */
    unsubscribeFromReview(reviewId) {
        ['comments', 'cursors', 'typing', 'presence'].forEach((type) => {
            const key = `review-${reviewId}-${type}`
            try { this.subscriptions.get(key)?.unsubscribe() } catch { }
            this.subscriptions.delete(key)
        })
    }

    // ── Publish ───────────────────────────────────────────────────────────

    /** Send a comment to the WebSocket controller (also persists via service). */
    sendComment(reviewId, commentData) {
        this._publish(`/app/review/${reviewId}/comment`, commentData)
    }

    /** Broadcast cursor position to collaborators. */
    sendCursor(reviewId, position) {
        this._publish(`/app/review/${reviewId}/cursor`, position)
    }

    /** Broadcast typing indicator. */
    sendTyping(reviewId, isTyping) {
        this._publish(`/app/review/${reviewId}/typing`, { typing: isTyping })
    }

    /** Broadcast join/leave presence. */
    sendPresence(reviewId, status) {
        this._publish(`/app/review/${reviewId}/presence`, { status })
    }

    // ── Private helpers ───────────────────────────────────────────────────

    _subscribe(key, destination, callback) {
        if (!this.isConnected) {
            console.warn(`[WS] Cannot subscribe to ${destination} — not connected`)
            return null
        }
        if (this.subscriptions.has(key)) return this.subscriptions.get(key)

        const sub = this.client.subscribe(destination, (msg) => {
            try {
                callback(JSON.parse(msg.body))
            } catch (e) {
                console.error('[WS] Parse error on', destination, e)
            }
        })
        this.subscriptions.set(key, sub)
        return sub
    }

    _publish(destination, payload) {
        if (!this.isConnected) {
            console.warn(`[WS] Cannot publish to ${destination} — not connected`)
            return
        }
        this.client.publish({ destination, body: JSON.stringify(payload) })
    }
}

// Singleton
const wsService = new WebSocketService()
export default wsService
