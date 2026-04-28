import React from 'react';

export default function AnalysisPanel({ log, analysis, isLoading }) {
  if (!log) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8 text-center text-slate-500">
        <svg className="w-16 h-16 mb-4 text-slate-700" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"></path></svg>
        <h3 className="text-lg font-medium text-slate-300 mb-2">No Log Selected</h3>
        <p className="text-sm">Click on any log entry to view its details and AI analysis.</p>
      </div>
    );
  }

  if (log.level !== 'ERROR') {
    return (
      <div className="p-6">
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-6 mb-6">
          <div className="flex items-start">
            <svg className="w-6 h-6 text-blue-400 mr-3 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <div>
              <h3 className="text-sm font-semibold text-blue-300 mb-1">Standard Log Entry</h3>
              <p className="text-xs text-blue-400/80">AI Analysis is only triggered for ERROR-level logs to conserve resources.</p>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Raw Message</h4>
            <div className="bg-slate-950 border border-slate-800 rounded-md p-4 font-mono text-sm text-slate-300 whitespace-pre-wrap break-words">
              {log.message}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="mb-8">
        <h2 className="text-xl font-bold text-white mb-2">AI Incident Report</h2>
        <div className="flex items-center space-x-2">
          <span className="text-xs font-medium text-indigo-400 bg-indigo-500/10 border border-indigo-500/20 px-2 py-1 rounded">{log.service}</span>
          <span className="text-xs text-slate-500 font-mono">{log.timestamp}</span>
        </div>
      </div>

      <div className="mb-6">
        <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Original Error</h4>
        <div className="bg-rose-500/5 border border-rose-500/20 rounded-md p-4 font-mono text-xs text-rose-300/90 whitespace-pre-wrap break-words">
          {log.message}
        </div>
      </div>

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <svg className="animate-spin h-8 w-8 text-indigo-500 mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p className="text-sm text-indigo-400 animate-pulse">LogSage AI is analyzing this error...</p>
        </div>
      ) : analysis ? (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
          
          <div className="flex justify-between items-center bg-slate-800/50 rounded-lg p-4 border border-slate-700/50">
            <div>
              <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Severity</h4>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border
                ${analysis.severity === 'HIGH' ? 'bg-rose-500/10 text-rose-400 border-rose-500/30' : 
                  analysis.severity === 'MEDIUM' ? 'bg-amber-500/10 text-amber-400 border-amber-500/30' : 
                  'bg-blue-500/10 text-blue-400 border-blue-500/30'}`}
              >
                {analysis.severity}
              </span>
            </div>
            
            <div className="text-right">
              <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Error Type</h4>
              <span className="font-mono text-sm text-slate-300">{analysis.errorType}</span>
            </div>
          </div>
          
          <div>
            <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Root Cause Analysis</h4>
            <div className="bg-slate-800/30 rounded-lg p-4 border border-slate-700/50 text-slate-300 text-sm leading-relaxed">
              {analysis.rootCause}
            </div>
          </div>
          
          <div>
            <h4 className="text-xs font-semibold text-emerald-500/80 uppercase tracking-wider mb-2 flex items-center">
              <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
              Suggested Fix
            </h4>
            <div className="bg-emerald-500/10 rounded-lg p-4 border border-emerald-500/20 text-emerald-100/90 text-sm leading-relaxed">
              {analysis.fixSuggestion}
            </div>
          </div>

        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-12 text-slate-500">
          <svg className="w-12 h-12 mb-3 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <p className="text-sm">Analysis pending or unavailable.</p>
        </div>
      )}
    </div>
  );
}
