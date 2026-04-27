import React, { useState } from 'react';

const EXAMPLE_LOGS = JSON.stringify([
  {
    "timestamp": "2026-04-28T00:05:23Z",
    "service": "auth-service",
    "level": "ERROR",
    "message": "NullPointerException at UserService.java:42 — Failed to authenticate user: token is null"
  },
  {
    "timestamp": "2026-04-28T00:05:24Z",
    "service": "auth-service",
    "level": "WARN",
    "message": "Connection pool exhausted, retrying database connection (attempt 3/5)"
  },
  {
    "timestamp": "2026-04-28T00:05:25Z",
    "service": "auth-service",
    "level": "ERROR",
    "message": "Circuit breaker OPEN for downstream service 'user-db'. Requests will be rejected for 30s"
  }
], null, 2);

export default function LogInput({ onAnalyze, isLoading }) {
  const [input, setInput] = useState(EXAMPLE_LOGS);
  const [error, setError] = useState('');

  const handleAnalyze = () => {
    setError('');
    try {
      const parsed = JSON.parse(input);
      const logs = Array.isArray(parsed) ? parsed : [parsed];

      // Basic client-side validation
      for (const log of logs) {
        if (!log.timestamp || !log.service || !log.level || !log.message) {
          throw new Error('Each log must have: timestamp, service, level, message');
        }
        if (!['INFO', 'WARN', 'ERROR'].includes(log.level)) {
          throw new Error(`Invalid log level "${log.level}". Must be INFO, WARN, or ERROR`);
        }
      }

      onAnalyze(logs);
    } catch (e) {
      if (e instanceof SyntaxError) {
        setError('Invalid JSON format. Please check your input.');
      } else {
        setError(e.message);
      }
    }
  };

  const handleLoadExample = () => {
    setInput(EXAMPLE_LOGS);
    setError('');
  };

  const handleClear = () => {
    setInput('');
    setError('');
  };

  return (
    <div className="card log-input-card">
      <div className="card-header">
        <h2 className="card-title">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <rect x="2" y="3" width="16" height="14" rx="2" stroke="currentColor" strokeWidth="1.5"/>
            <path d="M5 7h10M5 10h7M5 13h9" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          Log Input
        </h2>
        <div className="card-actions">
          <button className="btn-ghost" onClick={handleLoadExample} title="Load example logs">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 4h12M2 8h8M2 12h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            Example
          </button>
          <button className="btn-ghost" onClick={handleClear} title="Clear input">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            Clear
          </button>
        </div>
      </div>

      <div className="textarea-wrapper">
        <textarea
          id="log-input-textarea"
          className="log-textarea"
          value={input}
          onChange={(e) => { setInput(e.target.value); setError(''); }}
          placeholder='Paste your log JSON here... e.g. [{"timestamp": "...", "service": "...", "level": "ERROR", "message": "..."}]'
          spellCheck={false}
        />
        <div className="textarea-footer">
          <span className="char-count">{input.length} characters</span>
        </div>
      </div>

      {error && (
        <div className="error-banner">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5"/>
            <path d="M8 5v3M8 10v.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          {error}
        </div>
      )}

      <button
        id="analyze-button"
        className="btn-primary"
        onClick={handleAnalyze}
        disabled={isLoading || !input.trim()}
      >
        {isLoading ? (
          <>
            <span className="spinner"></span>
            Analyzing...
          </>
        ) : (
          <>
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 2l2.5 5 5.5.8-4 3.9.9 5.3L9 14.5 4.1 17l.9-5.3-4-3.9L6.5 7z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/>
            </svg>
            Analyze with AI
          </>
        )}
      </button>
    </div>
  );
}
