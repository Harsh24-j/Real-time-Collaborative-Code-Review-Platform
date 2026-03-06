/**
 * formatters.js — date/time formatting and display helpers.
 * Skills: JavaScript
 */

export function relativeTime(date) {
    if (!date) return ''
    const diff = (Date.now() - new Date(date).getTime()) / 1000
    if (diff < 60) return 'just now'
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
    if (diff < 2592000) return `${Math.floor(diff / 86400)}d ago`
    if (diff < 31536000) return `${Math.floor(diff / 2592000)}mo ago`
    return `${Math.floor(diff / 31536000)}y ago`
}

export function displayDate(date) {
    if (!date) return ''
    return new Intl.DateTimeFormat('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
    }).format(new Date(date))
}

export function displayDateTime(date) {
    if (!date) return ''
    return new Intl.DateTimeFormat('en-US', {
        month: 'short', day: 'numeric', year: 'numeric',
        hour: 'numeric', minute: '2-digit',
    }).format(new Date(date))
}

export function formatNumber(n) {
    return n == null ? '0' : new Intl.NumberFormat('en-US').format(n)
}

export function truncate(str, maxLen = 80) {
    if (!str) return ''
    return str.length <= maxLen ? str : str.slice(0, maxLen - 1) + '…'
}

export function capitalise(str) {
    if (!str) return ''
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

export function scoreColour(score) {
    if (score >= 8) return 'text-emerald-400'
    if (score >= 5) return 'text-amber-400'
    return 'text-red-400'
}
