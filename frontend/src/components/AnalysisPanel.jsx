import React from 'react';

const SEVERITY_STYLES = {
  HIGH:   'text-rose-400  bg-rose-500/10  border-rose-500/30',
  MEDIUM: 'text-amber-400 bg-amber-400/10 border-amber-400/30',
  LOW:    'text-sky-400   bg-sky-500/10   border-sky-500/30',
};

function SectionLabel({ children }) {
  return (
    <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500 mb-1.5">
      {children}
    </p>
  );
}

function InfoCard({ children, className = '' }) {
  return (
    <div className={`rounded-md border border-white/5 bg-white/[0.03] p-3.5 ${className}`}>
      {children}
    </div>
  );
}

function EmptyState({ icon, title, body }) {
  return (
    <div className="flex flex-col items-center justify-center h-full px-8 py-16 text-center gap-4">
      <div className="text-4xl opacity-30">{icon}</div>
      <div>
        <p className="text-sm font-semibold text-slate-300">{title}</p>
        <p className="text-xs text-slate-600 mt-1 leading-relaxed">{body}</p>
      </div>
    </div>
  );
}

export default function AnalysisPanel({ log, analysis, isLoading }) {

  /* ── Nothing selected ── */
  if (!log) {
    return (
      <div className="h-full">
        <EmptyState
          icon="↖"
          title="No log selected"
          body="Click any log entry in the stream to inspect it. ERROR logs will also show an AI incident report."
        />
      </div>
    );
  }

  /* ── Non-error log ── */
  if (log.level !== 'ERROR') {
    const levelColors = {
      INFO:  'border-sky-500/30   bg-sky-500/5   text-sky-300',
      WARN:  'border-amber-400/30 bg-amber-400/5 text-amber-300',
      DEBUG: 'border-slate-500/30 bg-slate-500/5 text-slate-300',
    };
    const cls = levelColors[log.level] ?? levelColors.DEBUG;

    return (
      <div className="p-5 flex flex-col gap-4">
        {/* Header */}
        <div className="flex items-center gap-2">
          <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-widest border ${cls}`}>
            {log.level}
          </span>
          <span className="text-xs text-slate-500 font-medium">{log.service}</span>
        </div>

        {/* Message */}
        <InfoCard>
          <SectionLabel>Message</SectionLabel>
          <p className="text-sm font-mono text-slate-300 leading-relaxed break-words">{log.message}</p>
        </InfoCard>

        {/* Timestamp */}
        <InfoCard>
          <SectionLabel>Timestamp</SectionLabel>
          <p className="text-sm font-mono text-slate-400">{log.timestamp}</p>
        </InfoCard>

        {/* Info notice */}
        <div className="flex gap-2.5 rounded-md bg-slate-800/50 border border-white/5 p-3">
          <svg className="w-4 h-4 text-slate-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-xs text-slate-500 leading-relaxed">
            AI analysis is only triggered for <span className="text-rose-400 font-semibold">ERROR</span>-level events. Select an error log to view the incident report.
          </p>
        </div>
      </div>
    );
  }

  /* ── ERROR log ── */
  return (
    <div className="flex flex-col h-full">
      {/* Panel header */}
      <div className="px-5 py-3.5 border-b border-white/5 bg-rose-500/5 shrink-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
          <span className="text-[10px] font-bold uppercase tracking-widest text-rose-400">Incident Report</span>
        </div>
        <p className="text-xs text-slate-500 font-medium truncate">{log.service}</p>
      </div>

      <div className="flex-1 overflow-y-auto p-5 flex flex-col gap-5">

        {/* Error message */}
        <InfoCard className="border-rose-500/15 bg-rose-500/[0.04]">
          <SectionLabel>Error Message</SectionLabel>
          <p className="text-xs font-mono text-rose-300/90 leading-relaxed break-words">{log.message}</p>
        </InfoCard>

        {/* AI content */}
        {isLoading ? (
          <div className="flex flex-col items-center gap-3 py-12">
            <div className="w-6 h-6 rounded-full border-2 border-violet-500 border-t-transparent animate-spin" />
            <p className="text-xs text-slate-500 animate-pulse">LogSage AI is analyzing…</p>
          </div>
        ) : analysis ? (
          <div className="flex flex-col gap-4">

            {/* Severity + Error Type */}
            <div className="grid grid-cols-2 gap-3">
              <InfoCard>
                <SectionLabel>Severity</SectionLabel>
                <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-bold uppercase tracking-wider border
                  ${SEVERITY_STYLES[analysis.severity?.toUpperCase()] ?? SEVERITY_STYLES.LOW}`}>
                  {analysis.severity}
                </span>
              </InfoCard>

              <InfoCard>
                <SectionLabel>Error Type</SectionLabel>
                <p className="text-xs font-mono text-slate-300 leading-relaxed break-all">{analysis.errorType}</p>
              </InfoCard>
            </div>

            {/* Root Cause */}
            <InfoCard>
              <SectionLabel>Root Cause</SectionLabel>
              <p className="text-sm text-slate-300 leading-relaxed">{analysis.rootCause}</p>
            </InfoCard>

            {/* Fix Suggestion */}
            <InfoCard className="border-emerald-500/20 bg-emerald-500/[0.04]">
              <div className="flex items-center gap-1.5 mb-1.5">
                <svg className="w-3.5 h-3.5 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                <SectionLabel>Suggested Fix</SectionLabel>
              </div>
              <p className="text-sm text-emerald-200/80 leading-relaxed">{analysis.fixSuggestion}</p>
            </InfoCard>

          </div>
        ) : (
          <div className="flex flex-col items-center gap-2 py-10 text-center">
            <p className="text-sm text-slate-500">Analysis not yet available.</p>
            <p className="text-xs text-slate-600">The worker may still be processing this event.</p>
          </div>
        )}

      </div>
    </div>
  );
}
