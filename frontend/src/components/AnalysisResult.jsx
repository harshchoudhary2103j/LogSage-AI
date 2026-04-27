import React from 'react';

const SEVERITY_CONFIG = {
  HIGH: { color: '#ef4444', bg: 'rgba(239, 68, 68, 0.15)', label: '🔴 HIGH' },
  MEDIUM: { color: '#f59e0b', bg: 'rgba(245, 158, 11, 0.15)', label: '🟡 MEDIUM' },
  LOW: { color: '#22c55e', bg: 'rgba(34, 197, 94, 0.15)', label: '🟢 LOW' },
};

export default function AnalysisResult({ result, error }) {
  if (error) {
    return (
      <div className="card result-card error-card">
        <div className="card-header">
          <h2 className="card-title error-title">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="#ef4444" strokeWidth="1.5"/>
              <path d="M10 6v5M10 13v.5" stroke="#ef4444" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            Analysis Failed
          </h2>
        </div>
        <p className="error-message">{error}</p>
      </div>
    );
  }

  if (!result) return null;

  const severity = SEVERITY_CONFIG[result.severity?.toUpperCase()] || SEVERITY_CONFIG.MEDIUM;

  return (
    <div className="card result-card" id="analysis-result">
      <div className="card-header">
        <h2 className="card-title">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 2l2.5 5 5.5.8-4 3.9.9 5.3L10 14.5 5.1 17l.9-5.3-4-3.9L7.5 7z" fill="url(#star-fill)" stroke="#8b5cf6" strokeWidth="1.2" strokeLinejoin="round"/>
            <defs>
              <linearGradient id="star-fill" x1="4" y1="2" x2="16" y2="17">
                <stop stopColor="#8b5cf6" stopOpacity="0.3"/>
                <stop offset="1" stopColor="#6366f1" stopOpacity="0.1"/>
              </linearGradient>
            </defs>
          </svg>
          AI Analysis Result
        </h2>
        <div
          className="severity-badge"
          style={{ color: severity.color, background: severity.bg, borderColor: severity.color + '40' }}
        >
          {severity.label}
        </div>
      </div>

      <div className="result-grid">
        <div className="result-item">
          <div className="result-label">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 3v5l3 2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5"/>
            </svg>
            Error Type
          </div>
          <div className="result-value">{result.errorType || result.error_type || '—'}</div>
        </div>

        <div className="result-item">
          <div className="result-label">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 1v6M4.5 4.5L8 8l3.5-3.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M2 10c0 3.3 2.7 5 6 5s6-1.7 6-5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            Root Cause
          </div>
          <div className="result-value">{result.rootCause || result.root_cause || '—'}</div>
        </div>

        <div className="result-item full-width">
          <div className="result-label">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M5 8h6M8 5v6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              <rect x="2" y="2" width="12" height="12" rx="3" stroke="currentColor" strokeWidth="1.5"/>
            </svg>
            Fix Suggestion
          </div>
          <div className="result-value fix-suggestion">{result.fixSuggestion || result.fix_suggestion || '—'}</div>
        </div>
      </div>
    </div>
  );
}
