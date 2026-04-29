import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { fetchLogs, fetchAnalysis } from '../services/api';
import MetricsBar from './MetricsBar';
import FiltersPanel from './FiltersPanel';
import LogsList from './LogsList';
import AnalysisPanel from './AnalysisPanel';

export default function DashboardPage() {
  const [logs, setLogs]                     = useState([]);
  const [loadingLogs, setLoadingLogs]       = useState(true);
  const [lastUpdated, setLastUpdated]       = useState(null);
  const [levelFilter, setLevelFilter]       = useState('');
  const [serviceFilter, setServiceFilter]   = useState('');
  const [selectedLog, setSelectedLog]       = useState(null);
  const [analysis, setAnalysis]             = useState(null);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);

  const loadLogs = useCallback(async () => {
    try {
      const data = await fetchLogs();
      setLogs(data);
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Failed to fetch logs:', err);
    } finally {
      setLoadingLogs(false);
    }
  }, []);

  useEffect(() => {
    loadLogs();
    const id = setInterval(loadLogs, 5000);
    return () => clearInterval(id);
  }, [loadLogs]);

  const handleLogSelect = useCallback(async (log) => {
    setSelectedLog(log);
    if (log.level !== 'ERROR') { setAnalysis(null); return; }
    setLoadingAnalysis(true);
    setAnalysis(null);
    try {
      setAnalysis(await fetchAnalysis(log.id));
    } catch (err) {
      console.error('Failed to fetch analysis:', err);
    } finally {
      setLoadingAnalysis(false);
    }
  }, []);

  const uniqueServices = useMemo(() =>
    [...new Set(logs.map(l => l.service))].sort()
  , [logs]);

  const levelWeight = { ERROR: 3, WARN: 2, INFO: 1, DEBUG: 0 };

  const displayedLogs = useMemo(() => {
    let res = logs;
    if (levelFilter)   res = res.filter(l => l.level   === levelFilter);
    if (serviceFilter) res = res.filter(l => l.service === serviceFilter);
    return [...res].sort((a, b) => {
      const wDiff = (levelWeight[b.level] ?? 0) - (levelWeight[a.level] ?? 0);
      return wDiff !== 0 ? wDiff : new Date(b.timestamp) - new Date(a.timestamp);
    });
  }, [logs, levelFilter, serviceFilter]);

  const formatLastUpdated = () => {
    if (!lastUpdated) return '—';
    return lastUpdated.toLocaleTimeString([], { hour12: false });
  };

  return (
    <div className="flex flex-col h-screen bg-[#0b0f1a] text-slate-200 overflow-hidden select-none">

      {/* ── Top Header ─────────────────────────────────────────────── */}
      <header className="flex items-center gap-8 px-6 py-0 bg-[#0f1523] border-b border-white/5 shrink-0 h-14">

        {/* Brand */}
        <div className="flex items-center gap-3 shrink-0">
          <div className="relative flex items-center justify-center w-8 h-8">
            <div className="absolute inset-0 rounded-lg bg-violet-600/20 border border-violet-500/30" />
            <svg className="relative w-4 h-4 text-violet-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2"
                d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <div>
            <span className="text-sm font-bold text-white tracking-tight">LogSage</span>
            <span className="text-sm font-bold text-violet-400 tracking-tight"> AI</span>
          </div>
        </div>

        {/* Divider */}
        <div className="h-5 w-px bg-white/10" />

        {/* Metrics Bar */}
        <div className="flex-1">
          <MetricsBar logs={logs} />
        </div>

        {/* Live indicator */}
        <div className="flex items-center gap-2 text-xs text-slate-500 shrink-0">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-60" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
          <span>Live · {formatLastUpdated()}</span>
        </div>

      </header>

      {/* ── Body ───────────────────────────────────────────────────── */}
      <div className="flex flex-1 overflow-hidden">

        {/* Left Sidebar */}
        <aside className="w-56 bg-[#0f1523] border-r border-white/5 shrink-0 overflow-y-auto">
          <FiltersPanel
            levelFilter={levelFilter}    setLevelFilter={setLevelFilter}
            serviceFilter={serviceFilter} setServiceFilter={setServiceFilter}
            uniqueServices={uniqueServices}
          />
        </aside>

        {/* Center — Logs Stream */}
        <main className="flex-1 flex flex-col overflow-hidden bg-[#0b0f1a]">
          {/* Sub-header */}
          <div className="flex items-center justify-between px-5 py-2.5 border-b border-white/5 bg-[#0c1120] shrink-0">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-slate-300 uppercase tracking-widest">Log Stream</span>
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-800 text-slate-400 border border-white/5">
                {displayedLogs.length}
              </span>
            </div>
            <span className="text-[11px] text-slate-600">Sorted by severity → time</span>
          </div>

          {/* Log items */}
          <div className="flex-1 overflow-y-auto p-3 space-y-1.5">
            {loadingLogs && logs.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full gap-3">
                <div className="w-6 h-6 rounded-full border-2 border-violet-500 border-t-transparent animate-spin" />
                <p className="text-sm text-slate-500">Connecting to log stream…</p>
              </div>
            ) : (
              <LogsList
                logs={displayedLogs}
                selectedLogId={selectedLog?.id}
                onLogSelect={handleLogSelect}
              />
            )}
          </div>
        </main>

        {/* Right — Analysis Panel */}
        <aside className="w-[380px] bg-[#0f1523] border-l border-white/5 shrink-0 overflow-y-auto">
          <AnalysisPanel log={selectedLog} analysis={analysis} isLoading={loadingAnalysis} />
        </aside>

      </div>
    </div>
  );
}
