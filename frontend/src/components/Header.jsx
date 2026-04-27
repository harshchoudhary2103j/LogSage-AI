import React from 'react';

export default function Header() {
  return (
    <header className="header">
      <div className="header-content">
        <div className="logo-section">
          <div className="logo-icon">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="32" height="32" rx="8" fill="url(#logo-gradient)" />
              <path d="M8 12h16M8 16h12M8 20h14" stroke="white" strokeWidth="2" strokeLinecap="round" />
              <circle cx="24" cy="22" r="4" fill="#34d399" stroke="white" strokeWidth="1.5" />
              <path d="M22.5 22l1 1 2-2" stroke="white" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
              <defs>
                <linearGradient id="logo-gradient" x1="0" y1="0" x2="32" y2="32">
                  <stop stopColor="#6366f1" />
                  <stop offset="1" stopColor="#8b5cf6" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <div>
            <h1 className="logo-text">LogSage<span className="logo-accent">AI</span></h1>
            <p className="tagline">AI-Powered DevOps Incident Analyzer</p>
          </div>
        </div>
        <div className="status-badge">
          <span className="status-dot"></span>
          Phase 1 — MVP
        </div>
      </div>
    </header>
  );
}
