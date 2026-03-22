import React, { useState, useEffect } from 'react'
import toast from 'react-hot-toast'
import {
    User, Mail, Star, Code2, MessageSquare, Award,
    Settings, Save, Lock, Sparkles,
} from 'lucide-react'
import { userAPI } from '../../services/api'
import { useAuthStore } from '../../hooks/useAuthStore'

/**
 * UserProfile â€” view and edit profile, change password, view stats.
 * Skills: JavaScript, Front-End Web Development
 */
function UserProfile() {
    const { user, updateUser } = useAuthStore()
    const [stats, setStats] = useState(null)
    const [editMode, setEditMode] = useState(false)
    const [loading, setLoading] = useState(false)
    const [loadingStats, setLoadingStats] = useState(true)
    const [profileForm, setProfileForm] = useState({ fullName: '', email: '' })
    const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirm: '' })
    const [activeTab, setActiveTab] = useState('overview') // overview | security

    useEffect(() => {
        if (user) setProfileForm({ fullName: user.fullName ?? '', email: user.email ?? '' })
        if (user?.id) {
            userAPI.getStats(user.id)
                .then(setStats)
                .catch(() => { })
                .finally(() => setLoadingStats(false))
        }
    }, [user])

    // â”€â”€ Save profile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    const handleSaveProfile = async () => {
        setLoading(true)
        try {
            const updated = await userAPI.updateProfile(profileForm)
            updateUser(updated)
            toast.success('Profile updated!')
            setEditMode(false)
        } catch {
            toast.error('Failed to update profile')
        } finally {
            setLoading(false)
        }
    }

    // â”€â”€ Change password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    const handleChangePassword = async (e) => {
        e.preventDefault()
        if (passwordForm.newPassword !== passwordForm.confirm) {
            toast.error('Passwords do not match')
            return
        }
        if (passwordForm.newPassword.length < 8) {
            toast.error('New password must be at least 8 characters')
            return
        }
        setLoading(true)
        try {
            await userAPI.changePassword({
                currentPassword: passwordForm.currentPassword,
                newPassword: passwordForm.newPassword,
            })
            toast.success('Password changed!')
            setPasswordForm({ currentPassword: '', newPassword: '', confirm: '' })
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to change password')
        } finally {
            setLoading(false)
        }
    }

    const pointsTier = (pts = 0) => {
        if (pts >= 500) return { label: 'Diamond', colour: 'text-cyan-300', icon: 'ðŸ’Ž' }
        if (pts >= 200) return { label: 'Gold', colour: 'text-amber-400', icon: 'ðŸ¥‡' }
        if (pts >= 100) return { label: 'Silver', colour: 'text-slate-300', icon: 'ðŸ¥ˆ' }
        return { label: 'Bronze', colour: 'text-amber-600', icon: 'ðŸ¥‰' }
    }
    const tier = pointsTier(user?.points)

    return (
        <div className="max-w-3xl mx-auto animate-fade-in space-y-6">

            {/* Profile hero */}
            <div className="card">
                <div className="flex items-start gap-6">

                    {/* Avatar */}
                    <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-600/40 to-slate-700
                           flex items-center justify-center text-3xl font-bold text-slate-100
                           border border-slate-600 flex-shrink-0">
                        {user?.fullName?.[0] ?? user?.username?.[0] ?? '?'}
                    </div>

                    {/* Info */}
                    <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                            <div>
                                <h1 className="page-title">{user?.fullName ?? user?.username}</h1>
                                <p className="text-slate-400 text-sm">@{user?.username}</p>
                                <p className={`text-sm mt-1 font-medium ${tier.colour}`}>
                                    {tier.icon} {tier.label} tier
                                </p>
                            </div>
                            <button
                                onClick={() => setEditMode(!editMode)}
                                className="btn btn-secondary btn-sm"
                            >
                                <Settings className="w-4 h-4" /> {editMode ? 'Cancel' : 'Edit'}
                            </button>
                        </div>

                        {/* Points + role */}
                        <div className="flex items-center gap-4 mt-3 text-sm text-slate-400">
                            <span className="flex items-center gap-1.5">
                                <Star className="w-4 h-4 text-amber-400" />
                                <strong className="text-slate-200">{user?.points ?? 0}</strong> points
                            </span>
                            <span className="flex items-center gap-1.5">
                                <Award className="w-4 h-4 text-blue-400" />
                                {user?.role ?? 'USER'}
                            </span>
                        </div>
                    </div>
                </div>

                {/* Edit form */}
                {editMode && (
                    <div className="mt-6 pt-6 border-t border-slate-800 space-y-4 animate-slide-up">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-1.5">Full Name</label>
                                <input
                                    className="input"
                                    value={profileForm.fullName}
                                    onChange={(e) => setProfileForm((p) => ({ ...p, fullName: e.target.value }))}
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-1.5">Email</label>
                                <input
                                    type="email"
                                    className="input"
                                    value={profileForm.email}
                                    onChange={(e) => setProfileForm((p) => ({ ...p, email: e.target.value }))}
                                />
                            </div>
                        </div>
                        <button onClick={handleSaveProfile} disabled={loading} className="btn btn-primary btn-sm">
                            {loading ? <><span className="spinner" /> Savingâ€¦</> : <><Save className="w-3.5 h-3.5" /> Save changes</>}
                        </button>
                    </div>
                )}
            </div>

            {/* Tabs */}
            <div className="flex border-b border-slate-800">
                {['overview', 'security'].map((t) => (
                    <button
                        key={t}
                        onClick={() => setActiveTab(t)}
                        className={`px-5 py-2.5 text-sm font-medium border-b-2 capitalize transition-colors ${activeTab === t
                                ? 'border-blue-500 text-blue-400'
                                : 'border-transparent text-slate-400 hover:text-slate-200'
                            }`}
                    >
                        {t}
                    </button>
                ))}
            </div>

            {/* Overview tab */}
            {activeTab === 'overview' && (
                <div className="grid grid-cols-2 gap-4 animate-fade-in">
                    {[
                        { label: 'Reviews Created', key: 'reviewsCreated', icon: <Code2 className="w-5 h-5 text-blue-400" />, bg: 'bg-blue-500/15' },
                        { label: 'Comments Written', key: 'commentsWritten', icon: <MessageSquare className="w-5 h-5 text-emerald-400" />, bg: 'bg-emerald-500/15' },
                        { label: 'Approved Reviews', key: 'approvedReviews', icon: <Sparkles className="w-5 h-5 text-amber-400" />, bg: 'bg-amber-500/15' },
                        { label: 'Total Points', key: null, icon: <Star className="w-5 h-5 text-amber-400" />, bg: 'bg-amber-500/15', value: user?.points ?? 0 },
                    ].map(({ label, key, icon, bg, value }) => (
                        <div key={label} className="card flex items-start gap-4">
                            <div className={`w-11 h-11 rounded-xl ${bg} flex items-center justify-center flex-shrink-0`}>
                                {icon}
                            </div>
                            <div>
                                <p className="text-xs text-slate-400">{label}</p>
                                <p className="text-2xl font-bold text-slate-100 mt-0.5">
                                    {loadingStats && key
                                        ? <span className="skeleton inline-block w-8 h-5 rounded" />
                                        : (value ?? stats?.[key] ?? 0)}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Security tab */}
            {activeTab === 'security' && (
                <div className="card animate-fade-in">
                    <h2 className="text-sm font-semibold text-slate-300 flex items-center gap-2 mb-6">
                        <Lock className="w-4 h-4" /> Change Password
                    </h2>
                    <form onSubmit={handleChangePassword} className="space-y-4 max-w-sm">
                        {[
                            { label: 'Current Password', field: 'currentPassword', autoComplete: 'current-password' },
                            { label: 'New Password', field: 'newPassword', autoComplete: 'new-password' },
                            { label: 'Confirm New', field: 'confirm', autoComplete: 'new-password' },
                        ].map(({ label, field, autoComplete }) => (
                            <div key={field}>
                                <label className="block text-xs font-medium text-slate-400 mb-1.5">{label}</label>
                                <input
                                    type="password"
                                    className="input"
                                    value={passwordForm[field]}
                                    onChange={(e) => setPasswordForm((p) => ({ ...p, [field]: e.target.value }))}
                                    autoComplete={autoComplete}
                                    required
                                />
                            </div>
                        ))}
                        <button type="submit" disabled={loading} className="btn btn-primary btn-sm">
                            {loading ? <><span className="spinner" /> Updatingâ€¦</> : <><Save className="w-3.5 h-3.5" /> Update password</>}
                        </button>
                    </form>
                </div>
            )}
        </div>
    )
}

export default UserProfile
