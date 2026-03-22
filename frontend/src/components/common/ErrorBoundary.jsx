import React from 'react'

/**
 * ErrorBoundary â€” catches any React render/lifecycle crash and renders a
 * friendly fallback instead of a blank page.
 *
 * Place this high in the tree (e.g. wrapping <App>) so it catches crashes
 * from any child component.
 *
 * Skills: JavaScript, Front-End Web Development
 */
class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props)
        this.state = { hasError: false, error: null }
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error }
    }

    componentDidCatch(error, info) {
        // In production this would send to Sentry / error-monitoring service
        console.error('[ErrorBoundary] Caught render error:', error, info)
    }

    handleReload = () => {
        this.setState({ hasError: false, error: null })
        window.location.href = '/dashboard'
    }

    render() {
        if (!this.state.hasError) return this.props.children

        return (
            <div className="min-h-screen bg-slate-950 flex items-center justify-center px-4">
                <div className="max-w-md w-full text-center">
                    {/* Icon */}
                    <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-red-900/30 border border-red-500/30 mb-6">
                        <svg className="w-10 h-10 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                                d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
                        </svg>
                    </div>

                    <h1 className="text-2xl font-bold text-white mb-2">Something went wrong</h1>
                    <p className="text-slate-400 mb-6 text-sm leading-relaxed">
                        An unexpected error occurred. Our team has been notified.
                        {import.meta.env.DEV && this.state.error && (
                            <span className="block mt-2 font-mono text-xs text-red-400 bg-red-950/40 rounded p-2 text-left break-all">
                                {this.state.error.message}
                            </span>
                        )}
                    </p>

                    <div className="flex gap-3 justify-center">
                        <button
                            onClick={this.handleReload}
                            className="px-5 py-2.5 bg-violet-600 hover:bg-violet-500 text-white text-sm font-medium rounded-lg transition-colors"
                        >
                            Go to Dashboard
                        </button>
                        <button
                            onClick={() => window.location.reload()}
                            className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 text-sm font-medium rounded-lg transition-colors"
                        >
                            Reload Page
                        </button>
                    </div>
                </div>
            </div>
        )
    }
}

export default ErrorBoundary
