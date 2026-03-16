import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Code2, Sparkles, ArrowLeft, Upload } from 'lucide-react'
import { reviewAPI } from '../../services/api'
import BackgroundWrapper from '../common/BackgroundWrapper'

const LANGUAGES = [
    'java', 'python', 'javascript', 'typescript', 'go', 'rust',
    'cpp', 'csharp', 'php', 'ruby', 'kotlin', 'swift', 'scala',
]

/**
 * CreateReview — submit new code for AI-powered review.
 * Skills: JavaScript, Front-End Web Development, Full-Stack Web Development
 */
function CreateReview() {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [form, setForm] = useState({
        title: '',
        description: '',
        language: 'java',
        codeContent: '',
        repositoryUrl: '',
    })

    const handleChange = (e) =>
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!form.codeContent.trim()) {
            toast.error('Code content is required')
            return
        }
        if (form.codeContent.length > 100_000) {
            toast.error('Code exceeds 100 000 character limit')
            return
        }
        setLoading(true)
        try {
            const review = await reviewAPI.create(form)
            toast.success('Review created — AI analysis started!')
            navigate(`/reviews/${review.id}`)
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to create review')
        } finally {
            setLoading(false)
        }
    }

    const charCount = form.codeContent.length
    const overLimit = charCount > 100_000

    return (
        <BackgroundWrapper variant="code2">
            <div className="max-w-4xl mx-auto animate-slide-up relative z-10 p-4">

            {/* Header */}
            <div className="flex items-center gap-4 mb-8">
                <button onClick={() => navigate('/reviews')} className="btn btn-secondary btn-sm">
                    <ArrowLeft className="w-4 h-4" />
                </button>
                <div>
                    <h1 className="page-title">Submit Code Review</h1>
                    <p className="page-subtitle">Paste your code and receive AI-powered analysis instantly</p>
                </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">

                {/* Meta card */}
                <div className="glass-card-dark p-6 space-y-5 shadow-xl">
                    <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider">
                        Review Details
                    </h2>

                    {/* Title */}
                    <div>
                        <label className="block text-sm font-medium text-slate-300 mb-1.5">
                            Title <span className="text-red-400">*</span>
                        </label>
                        <input
                            id="review-title"
                            name="title"
                            value={form.title}
                            onChange={handleChange}
                            className="input"
                            placeholder="e.g. Auth service JWT implementation"
                            required
                            minLength={5}
                            maxLength={200}
                        />
                    </div>

                    {/* Description */}
                    <div>
                        <label className="block text-sm font-medium text-slate-300 mb-1.5">
                            Description <span className="text-slate-500 text-xs">(optional)</span>
                        </label>
                        <textarea
                            id="review-description"
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            className="input resize-none"
                            rows={3}
                            placeholder="What should reviewers focus on? Any context about the code?"
                            maxLength={2000}
                        />
                    </div>

                    {/* Language + Repo URL */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Language <span className="text-red-400">*</span>
                            </label>
                            <select
                                id="review-language"
                                name="language"
                                value={form.language}
                                onChange={handleChange}
                                className="input"
                                required
                            >
                                {LANGUAGES.map((l) => (
                                    <option key={l} value={l}>{l.charAt(0).toUpperCase() + l.slice(1)}</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-1.5">
                                Repository URL <span className="text-slate-500 text-xs">(optional)</span>
                            </label>
                            <input
                                id="review-repo"
                                name="repositoryUrl"
                                type="url"
                                value={form.repositoryUrl}
                                onChange={handleChange}
                                className="input"
                                placeholder="https://github.com/…"
                            />
                        </div>
                    </div>
                </div>

                {/* Code card */}
                <div className="glass-card-dark p-6 space-y-4 shadow-xl">
                    <div className="flex items-center justify-between">
                        <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider">
                            Code <span className="text-red-400">*</span>
                        </h2>
                        <span className={`text-xs font-mono ${overLimit ? 'text-red-400' : 'text-slate-500'}`}>
                            {charCount.toLocaleString()} / 100 000
                        </span>
                    </div>

                    <textarea
                        id="review-code"
                        name="codeContent"
                        value={form.codeContent}
                        onChange={handleChange}
                        className={`input font-mono text-sm resize-none leading-relaxed ${overLimit ? 'input-error' : ''}`}
                        rows={24}
                        placeholder={`// Paste your ${form.language} code here…`}
                        required
                        spellCheck={false}
                    />

                    {/* AI hint bar */}
                    <div className="flex items-center gap-2 text-xs text-slate-500 bg-slate-800/50 rounded-lg px-4 py-2.5">
                        <Sparkles className="w-3.5 h-3.5 text-amber-400 flex-shrink-0" />
                        After submission, GPT-4 will automatically analyse your code for security issues,
                        performance bottlenecks, bugs, and best practice violations.
                    </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-end gap-3 pb-8">
                    <button type="button" onClick={() => navigate('/reviews')} className="btn btn-secondary">
                        Cancel
                    </button>
                    <button
                        type="submit"
                        disabled={loading || overLimit}
                        className="btn btn-primary btn-lg"
                    >
                        {loading ? (
                            <><span className="spinner" /> Submitting…</>
                        ) : (
                            <><Upload className="w-4 h-4" /> Submit for Review</>
                        )}
                    </button>
                </div>
            </form>
            </div>
        </BackgroundWrapper>
    )
}

export default CreateReview
