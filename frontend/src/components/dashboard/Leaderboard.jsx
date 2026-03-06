import React, { useState, useEffect } from 'react'
import { Trophy, Medal, Star, Award, TrendingUp, Crown } from 'lucide-react'
import { userAPI } from '../../services/api'
import { useAuthStore } from '../../hooks/useAuthStore'

/**
 * Leaderboard — top users by points with tier badges.
 * Skills: JavaScript, Front-End Web Development
 */
function Leaderboard() {
    const { user: me } = useAuthStore()
    const [users, setUsers] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        userAPI.getLeaderboard().then(setUsers).catch(() => { }).finally(() => setLoading(false))
    }, [])

    // ── Rank helpers ───────────────────────────────────────────────────────

    const RANK_STYLE = {
        0: { icon: <Crown className="w-5 h-5 text-amber-400" />, ring: 'ring-2 ring-amber-400/60', bg: 'from-amber-500/20 to-amber-600/5' },
        1: { icon: <Medal className="w-5 h-5 text-slate-300" />, ring: 'ring-2 ring-slate-400/40', bg: 'from-slate-400/10 to-slate-500/5' },
        2: { icon: <Medal className="w-5 h-5 text-amber-600" />, ring: 'ring-2 ring-amber-700/40', bg: 'from-amber-700/15 to-amber-800/5' },
    }

    const pointsTier = (pts) => {
        if (pts >= 500) return { label: 'Diamond', colour: 'text-cyan-300', icon: '💎' }
        if (pts >= 200) return { label: 'Gold', colour: 'text-amber-400', icon: '🥇' }
        if (pts >= 100) return { label: 'Silver', colour: 'text-slate-300', icon: '🥈' }
        return { label: 'Bronze', colour: 'text-amber-600', icon: '🥉' }
    }

    const maxPoints = users[0]?.points ?? 1

    // ── Podium (top 3) ─────────────────────────────────────────────────────

    const Podium = ({ user: u, rank }) => {
        const rs = RANK_STYLE[rank]
        const tier = pointsTier(u.points)
        const isMe = u.id === me?.id
        const heights = ['h-28', 'h-20', 'h-16']

        return (
            <div className="flex flex-col items-center gap-3">
                {/* Avatar */}
                <div className={`relative w-16 h-16 rounded-full bg-gradient-to-br ${rs.bg}
                         flex items-center justify-center text-2xl font-bold
                         border border-slate-700 ${rs.ring} ${isMe ? 'scale-110' : ''}`}>
                    {u.fullName?.[0] ?? u.username?.[0] ?? '?'}
                    {isMe && (
                        <span className="absolute -top-1 -right-1 text-xs bg-blue-600 text-white
                             rounded-full px-1 font-bold">You</span>
                    )}
                </div>

                {/* Name + tier */}
                <div className="text-center">
                    <p className="text-sm font-semibold text-slate-200 truncate max-w-[90px]">
                        {u.username}
                    </p>
                    <p className={`text-xs ${tier.colour}`}>{tier.icon} {tier.label}</p>
                </div>

                {/* Points bar */}
                <div className={`w-20 ${heights[rank]} rounded-t-lg bg-gradient-to-t
                         ${rank === 0 ? 'from-amber-600/40 to-amber-400/20 border-t-2 border-amber-400/60'
                        : rank === 1 ? 'from-slate-600/40 to-slate-400/20 border-t-2 border-slate-400/60'
                            : 'from-amber-800/40 to-amber-700/20 border-t-2 border-amber-700/60'}
                         flex flex-col items-center justify-start pt-2`}>
                    <span className="text-xs font-mono font-bold text-slate-200">
                        {u.points.toLocaleString()}
                    </span>
                    <span className="text-[9px] text-slate-400">pts</span>
                </div>

                {/* Rank label */}
                <div className="flex items-center gap-1">{rs.icon} <span className="text-xs text-slate-400">#{rank + 1}</span></div>
            </div>
        )
    }

    // ── Rest of table ──────────────────────────────────────────────────────

    const RankRow = ({ user: u, rank }) => {
        const tier = pointsTier(u.points)
        const pct = maxPoints ? Math.round((u.points / maxPoints) * 100) : 0
        const isMe = u.id === me?.id

        return (
            <div className={`flex items-center gap-4 p-3 rounded-lg transition-all
                       ${isMe
                    ? 'bg-blue-500/10 border border-blue-500/30'
                    : 'bg-slate-800/40 hover:bg-slate-800/70 border border-transparent hover:border-slate-700'}`}>
                {/* Rank */}
                <span className="w-8 text-center text-sm font-mono font-bold text-slate-500">
                    #{rank + 1}
                </span>

                {/* Avatar */}
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-blue-600/30 to-slate-700
                         flex items-center justify-center text-sm font-bold text-slate-200 flex-shrink-0">
                    {u.fullName?.[0] ?? u.username?.[0] ?? '?'}
                </div>

                {/* Name + bar */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                        <span className="text-sm font-medium text-slate-200 truncate">{u.username}</span>
                        {isMe && <span className="text-[10px] badge badge-blue">You</span>}
                        <span className={`text-xs ${tier.colour} ml-auto`}>{tier.icon}</span>
                    </div>
                    <div className="h-1 bg-slate-800 rounded-full mt-1.5 overflow-hidden">
                        <div className="h-full bg-blue-500 rounded-full transition-all duration-700"
                            style={{ width: `${pct}%` }} />
                    </div>
                </div>

                {/* Points */}
                <span className="text-sm font-mono font-bold text-slate-200 flex-shrink-0">
                    {u.points.toLocaleString()}
                    <span className="text-xs text-slate-500 ml-1">pts</span>
                </span>
            </div>
        )
    }

    const topThree = users.slice(0, 3)
    const rest = users.slice(3)

    return (
        <div className="max-w-2xl mx-auto animate-fade-in space-y-8">

            {/* Header */}
            <div className="text-center">
                <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl
                        bg-amber-500/20 border border-amber-500/30 mb-4">
                    <Trophy className="w-7 h-7 text-amber-400" />
                </div>
                <h1 className="page-title">Leaderboard</h1>
                <p className="page-subtitle">Top contributors ranked by review points</p>
            </div>

            {loading ? (
                <div className="space-y-3">
                    {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className="skeleton h-14 rounded-lg" />
                    ))}
                </div>
            ) : users.length === 0 ? (
                <div className="card text-center py-12 text-slate-400">
                    <Trophy className="w-12 h-12 mx-auto mb-3 opacity-30" />
                    No users yet — start reviewing to earn points!
                </div>
            ) : (
                <>
                    {/* Podium */}
                    {topThree.length >= 2 && (
                        <div className="card">
                            <div className="flex items-end justify-center gap-6 pt-4">
                                {topThree[1] && <Podium user={topThree[1]} rank={1} />}
                                {topThree[0] && <Podium user={topThree[0]} rank={0} />}
                                {topThree[2] && <Podium user={topThree[2]} rank={2} />}
                            </div>
                        </div>
                    )}

                    {/* Full rankings */}
                    <div className="card space-y-2">
                        <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-4">
                            Full Rankings
                        </h2>
                        {users.map((u, idx) => (
                            <RankRow key={u.id} user={u} rank={idx} />
                        ))}
                    </div>

                    {/* Points guide */}
                    <div className="card-sm">
                        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
                            <Star className="w-3.5 h-3.5 inline mr-1 text-amber-400" /> How to Earn Points
                        </p>
                        <div className="grid grid-cols-2 gap-2 text-xs text-slate-400">
                            {[
                                ['Submit a review', '+10 pts'],
                                ['Add a comment', '+5 pts'],
                                ['Review approved', '+20 pts'],
                                ['Resolve a comment', '+15 pts'],
                            ].map(([action, pts]) => (
                                <div key={action} className="flex justify-between bg-slate-800/50 rounded px-3 py-2">
                                    <span>{action}</span>
                                    <span className="text-amber-400 font-mono font-bold">{pts}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}

export default Leaderboard
