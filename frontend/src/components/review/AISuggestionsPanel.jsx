import React, { useState, useEffect } from 'react'
import { Sparkles, AlertTriangle, AlertCircle, Info, RefreshCw } from 'lucide-react'
import { analyticsAPI } from '../../services/api'

const SEVERITY_ICON = { CRITICAL: AlertTriangle, WARNING: AlertCircle, INFO: Info }
const SEVERITY_CLASS = {
    CRITICAL: 'severity-critical',
    WARNING: 'severity-warning',
    INFO: 'severity-info',
}
const CATEGORY_COLOUR = {
    SECURITY: 'text-red-400',
    PERFORMANCE: 'text-orange-400',
    BUGS: 'text-amber-400',
    STYLE: 'text-blue-400',
    BEST_PRACTICE: 'text-emerald-400',
}

/**
 * AISuggestionsPanel — loads and displays AI suggestions for a review.
 * Skills: JavaScript, Front-End Web Development
 */
function AISuggestionsPanel({ reviewId, critical = 0, warnings = 0, info = 0 }) {
    const [metrics, setMetrics] = useState(null)
    const [loading, setLoading] = useState(true)

    const loadMetrics = async () => {
        try {
            setLoading(true)
            const data = await analyticsAPI.getReviewMetrics(reviewId)
            setMetrics(data)
        } catch {
            setMetrics(null)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { loadMetrics() }, [reviewId])

    // ── Summary row ──────────────────────────────────────────────────────

    const SummaryPill = ({ count, label, colorClass }) =>
        count > 0 ? (
            <div className={`flex flex-col items-center p-3 rounded-lg bg-slate-800/60 border border-slate-700 ${colorClass}`}>
                <span className="text-xl font-bold">{count}</span>
                <span className="text-xs text-slate-400 mt-0.5">{label}</span>
            </div>
        ) : null

    return (
        <div className="p-4 space-y-5">

            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <Sparkles className="w-4 h-4 text-amber-400" />
                    <span className="text-sm font-semibold text-slate-200">AI Analysis</span>
                </div>
                <button onClick={loadMetrics} className="btn btn-ghost btn-sm">
                    <RefreshCw className="w-3.5 h-3.5" />
                </button>
            </div>

            {/* Severity summary */}
            <div className="grid grid-cols-3 gap-2">
                <SummaryPill count={critical} label="Critical" colorClass="text-red-400" />
                <SummaryPill count={warnings} label="Warnings" colorClass="text-amber-400" />
                <SummaryPill count={info} label="Info" colorClass="text-blue-400" />
            </div>

            {(critical === 0 && warnings === 0 && info === 0) && (
                <div className="flex flex-col items-center py-8 text-slate-500 text-sm">
                    <Sparkles className="w-10 h-10 mb-2 text-emerald-500 opacity-70" />
                    <p className="font-medium text-emerald-400">No issues detected!</p>
                    <p className="text-xs mt-1">Your code looks clean 🎉</p>
                </div>
            )}

            {/* Metrics / distribution */}
            {loading ? (
                <div className="space-y-3">
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="skeleton h-12 rounded-lg" />
                    ))}
                </div>
            ) : metrics ? (
                <>
                    {/* Category distribution */}
                    {metrics.aiCategoryDistribution &&
                        Object.keys(metrics.aiCategoryDistribution).length > 0 && (
                            <div>
                                <p className="text-xs font-medium text-slate-400 uppercase tracking-wider mb-3">
                                    By Category
                                </p>
                                <div className="space-y-2">
                                    {Object.entries(metrics.aiCategoryDistribution).map(([cat, count]) => {
                                        const total = Object.values(metrics.aiCategoryDistribution)
                                            .reduce((s, v) => s + Number(v), 0)
                                        const pct = total ? Math.round((Number(count) / total) * 100) : 0
                                        return (
                                            <div key={cat}>
                                                <div className="flex justify-between text-xs mb-1">
                                                    <span className={`font-medium ${CATEGORY_COLOUR[cat] ?? 'text-slate-300'}`}>
                                                        {cat}
                                                    </span>
                                                    <span className="text-slate-400">{count} ({pct}%)</span>
                                                </div>
                                                <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
                                                    <div
                                                        className="h-full bg-blue-500 rounded-full transition-all duration-700"
                                                        style={{ width: `${pct}%` }}
                                                    />
                                                </div>
                                            </div>
                                        )
                                    })}
                                </div>
                            </div>
                        )}

                    {/* Severity breakdown */}
                    {metrics.aiSeverityDistribution && (
                        <div>
                            <p className="text-xs font-medium text-slate-400 uppercase tracking-wider mb-3">
                                By Severity
                            </p>
                            <div className="space-y-2">
                                {Object.entries(metrics.aiSeverityDistribution).map(([sev, count]) => {
                                    const Icon = SEVERITY_ICON[sev] ?? Info
                                    return (
                                        <div key={sev} className="flex items-center justify-between
                                               card-sm py-2 px-3">
                                            <span className={`flex items-center gap-2 text-xs ${SEVERITY_CLASS[sev] ?? ''}`}>
                                                <Icon className="w-3.5 h-3.5" />
                                                {sev}
                                            </span>
                                            <span className="text-xs font-mono text-slate-300">{count}</span>
                                        </div>
                                    )
                                })}
                            </div>
                        </div>
                    )}
                </>
            ) : (
                <p className="text-xs text-slate-500 text-center py-4">
                    Metrics unavailable — analysis may still be running
                </p>
            )}

            <p className="text-[10px] text-slate-600 text-center">
                Powered by GPT-4 · Results appear automatically after creation
            </p>
        </div>
    )
}

export default AISuggestionsPanel
