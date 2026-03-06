/**
 * constants.js — app-wide constants for status, languages, badges, etc.
 * Skills: JavaScript
 */

// ── Review status ─────────────────────────────────────────────────────────

export const REVIEW_STATUSES = ['OPEN', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'CLOSED']

export const STATUS_BADGE_CLASS = {
    OPEN: 'badge badge-blue',
    IN_REVIEW: 'badge badge-yellow',
    APPROVED: 'badge badge-green',
    REJECTED: 'badge badge-red',
    CLOSED: 'badge badge-gray',
}

export const STATUS_LABELS = {
    OPEN: 'Open',
    IN_REVIEW: 'In Review',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    CLOSED: 'Closed',
}

// ── Languages ─────────────────────────────────────────────────────────────

export const LANGUAGES = [
    'java', 'python', 'javascript', 'typescript', 'go', 'rust',
    'cpp', 'csharp', 'php', 'ruby', 'kotlin', 'swift', 'scala',
]

export const LANGUAGE_COLOUR = {
    java: 'text-amber-400',
    python: 'text-blue-400',
    javascript: 'text-yellow-400',
    typescript: 'text-blue-500',
    go: 'text-cyan-400',
    rust: 'text-orange-400',
    kotlin: 'text-purple-400',
}

// ── AI severity ───────────────────────────────────────────────────────────

export const SEVERITY_BADGE_CLASS = {
    CRITICAL: 'severity-critical',
    WARNING: 'severity-warning',
    INFO: 'severity-info',
}

export const SEVERITY_WEIGHT = { CRITICAL: 3, WARNING: 2, INFO: 1 }

// ── Gamification ──────────────────────────────────────────────────────────

export const POINT_VALUES = {
    SUBMIT_REVIEW: 10,
    ADD_COMMENT: 5,
    REVIEW_APPROVED: 20,
    RESOLVE_COMMENT: 15,
}

export const TIERS = [
    { label: 'Bronze', min: 0, icon: '🥉', colour: 'text-amber-600' },
    { label: 'Silver', min: 100, icon: '🥈', colour: 'text-slate-300' },
    { label: 'Gold', min: 200, icon: '🥇', colour: 'text-amber-400' },
    { label: 'Diamond', min: 500, icon: '💎', colour: 'text-cyan-300' },
]

export function getTier(points) {
    return [...TIERS].reverse().find((t) => points >= t.min) ?? TIERS[0]
}

// ── Pagination ────────────────────────────────────────────────────────────

export const PAGE_SIZES = [5, 10, 20, 50]
export const DEFAULT_PAGE_SIZE = 10

// ── Code editor max size ──────────────────────────────────────────────────

export const MAX_CODE_LENGTH = 100_000
