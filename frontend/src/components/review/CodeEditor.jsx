import React, { useState, useRef, useEffect, useCallback } from 'react'
import Editor from '@monaco-editor/react'
import { MessageSquare, Sparkles, ChevronDown } from 'lucide-react'

/**
 * CodeEditor â€” Monaco Editor with comment/AI-suggestion decorations.
 * Skills: JavaScript, Front-End Web Development
 */
function CodeEditor({
    code,
    language = 'javascript',
    readOnly = false,
    onCodeChange,
    comments = [],
    aiSuggestions = [],
    onLineClick,
    collaboratorCursors = [],   // [{ username, line, column }]
}) {
    const editorRef = useRef(null)
    const monacoRef = useRef(null)
    const decorationIds = useRef([])
    const [selectedLine, setSelectedLine] = useState(null)

    // â”€â”€ Editor mount â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    const handleMount = useCallback((editor, monaco) => {
        editorRef.current = editor
        monacoRef.current = monaco

        // Gutter click â†’ select line for comments
        editor.onMouseDown((e) => {
            if (e.target.type === monaco.editor.MouseTargetType.GUTTER_LINE_NUMBERS ||
                e.target.type === monaco.editor.MouseTargetType.GUTTER_GLYPH_MARGIN) {
                const line = e.target.position?.lineNumber
                if (line) {
                    setSelectedLine(line)
                    onLineClick?.(line)
                }
            }
        })

        applyDecorations(editor, monaco)
    }, []) // eslint-disable-line

    // â”€â”€ Decorations â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    const applyDecorations = useCallback((editor, monaco) => {
        if (!editor || !monaco) return

        const newDecs = []

        // Comment highlights (blue left border)
        comments.forEach((c) => {
            if (!c.lineNumber) return
            newDecs.push({
                range: new monaco.Range(c.lineNumber, 1, c.lineNumber, 1),
                options: {
                    isWholeLine: true,
                    className: 'bg-blue-500/10 border-l-2 border-blue-500',
                    glyphMarginClassName: 'text-blue-400',
                    glyphMarginHoverMessage: { value: `ðŸ’¬ ${c.username ?? 'User'}: ${c.commentText}` },
                },
            })
        })

        // AI suggestion highlights (colour by severity)
        aiSuggestions?.forEach((s) => {
            if (!s.lineStart) return
            const colours = {
                CRITICAL: { bg: 'bg-red-500/15 border-l-2 border-red-500', glyph: 'ðŸ”´' },
                WARNING: { bg: 'bg-amber-500/15 border-l-2 border-amber-500', glyph: 'ðŸŸ¡' },
                INFO: { bg: 'bg-blue-500/10 border-l-2 border-blue-400', glyph: 'ðŸ”µ' },
            }
            const c = colours[s.severity] ?? colours.INFO
            newDecs.push({
                range: new monaco.Range(s.lineStart, 1, s.lineEnd ?? s.lineStart, 1),
                options: {
                    isWholeLine: true,
                    className: c.bg,
                    glyphMarginHoverMessage: { value: `${c.glyph} **[${s.severity}]** ${s.suggestion}` },
                },
            })
        })

        decorationIds.current = editor.deltaDecorations(decorationIds.current, newDecs)
    }, [comments, aiSuggestions])

    useEffect(() => {
        if (editorRef.current && monacoRef.current) {
            applyDecorations(editorRef.current, monacoRef.current)
        }
    }, [applyDecorations])

    // â”€â”€ Editor options â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    const options = {
        readOnly,
        fontSize: 14,
        fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
        fontLigatures: true,
        lineNumbers: 'on',
        glyphMargin: true,
        minimap: { enabled: true, scale: 1 },
        folding: true,
        renderLineHighlight: 'gutter',
        scrollBeyondLastLine: false,
        automaticLayout: true,
        wordWrap: 'on',
        smoothScrolling: true,
        cursorBlinking: 'smooth',
        cursorSmoothCaretAnimation: 'on',
        mouseWheelZoom: true,
        contextmenu: true,
        renderWhitespace: 'selection',
        bracketPairColorization: { enabled: true },
    }

    return (
        <div className="relative h-full flex flex-col">
            {/* Stats bar */}
            <div className="absolute top-2 right-3 z-10 flex items-center gap-3
                      bg-slate-800/90 backdrop-blur-sm border border-slate-700
                      rounded-lg px-3 py-1.5 text-xs text-slate-300">
                <span className="flex items-center gap-1.5">
                    <MessageSquare className="w-3.5 h-3.5 text-blue-400" />
                    {comments.length}
                </span>
                <span className="flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                    {aiSuggestions.length}
                </span>
                {collaboratorCursors.length > 0 && (
                    <span className="flex items-center gap-1.5 text-emerald-400">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                        {collaboratorCursors.length} live
                    </span>
                )}
            </div>

            {/* Monaco */}
            <div className="flex-1 editor-wrapper">
                <Editor
                    height="100%"
                    theme="vs-dark"
                    language={language.toLowerCase()}
                    value={code}
                    onChange={onCodeChange}
                    onMount={handleMount}
                    options={options}
                    loading={
                        <div className="flex items-center justify-center h-full bg-slate-900 text-slate-400 text-sm">
                            <span className="spinner mr-2" /> Loading editorâ€¦
                        </div>
                    }
                />
            </div>

            {/* Selected-line tooltip */}
            {selectedLine && readOnly && (
                <div className="absolute bottom-4 left-4 animate-fade-in
                        flex items-center gap-2
                        bg-blue-600 text-white text-xs px-3 py-2 rounded-lg shadow-lg
                        shadow-blue-600/30">
                    <ChevronDown className="w-3.5 h-3.5" />
                    Line {selectedLine} â€” click gutter to comment
                </div>
            )}
        </div>
    )
}

export default CodeEditor
