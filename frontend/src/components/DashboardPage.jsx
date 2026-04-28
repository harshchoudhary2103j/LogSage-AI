import React, { useState, useEffect, useMemo } from 'react';
import { fetchLogs, fetchAnalysis } from '../services/api';
import MetricsBar from './MetricsBar';
import FiltersPanel from './FiltersPanel';
import LogsList from './LogsList';
import AnalysisPanel from './AnalysisPanel';

export default function DashboardPage() {
  const [logs, setLogs] = useState([]);
  const [loadingLogs, setLoadingLogs] = useState(true);
  
  // Filters state
  const [levelFilter, setLevelFilter] = useState('');
  const [serviceFilter, setServiceFilter] = useState('');
  
  const [selectedLog, setSelectedLog] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);

  // Poll for logs
  useEffect(() => {
    loadLogs();
    const interval = setInterval(loadLogs, 5000);
    return () => clearInterval(interval);
  }, []);

  const loadLogs = async () => {
    try {
      // Fetch all to allow local filtering + metric calculation
      const data = await fetchLogs();
      setLogs(data);
    } catch (error) {
      console.error('Error fetching logs:', error);
    } finally {
      setLoadingLogs(false);
    }
  };

  const handleLogSelect = async (log) => {
    setSelectedLog(log);
    
    if (log.level === 'ERROR') {
      setLoadingAnalysis(true);
      setAnalysis(null);
      try {
        const data = await fetchAnalysis(log.id);
        setAnalysis(data);
      } catch (error) {
        console.error('Error fetching analysis:', error);
      } finally {
        setLoadingAnalysis(false);
      }
    } else {
      setAnalysis(null);
    }
  };

  // Derive services list for the filter dropdown
  const uniqueServices = useMemo(() => {
    const services = new Set(logs.map(l => l.service));
    return Array.from(services).sort();
  }, [logs]);

  // Apply filters and sort logic (ERROR/WARN first, then by timestamp descending)
  const filteredAndSortedLogs = useMemo(() => {
    let result = logs;
    
    if (levelFilter) {
      result = result.filter(log => log.level === levelFilter);
    }
    if (serviceFilter) {
      result = result.filter(log => log.service === serviceFilter);
    }

    // Sort: severity first (ERROR=3, WARN=2, INFO=1, DEBUG=0), then time
    const levelWeight = { ERROR: 3, WARN: 2, INFO: 1, DEBUG: 0 };
    
    return result.sort((a, b) => {
      const weightA = levelWeight[a.level] || 0;
      const weightB = levelWeight[b.level] || 0;
      
      if (weightA !== weightB) {
        return weightB - weightA; // Higher severity first
      }
      
      // If same severity, sort by timestamp desc
      return new Date(b.timestamp) - new Date(a.timestamp);
    });
  }, [logs, levelFilter, serviceFilter]);

  return (
    <div className="flex flex-col h-screen bg-slate-950 text-slate-200">
      {/* Top Header / Metrics */}
      <header className="flex items-center justify-between px-6 py-4 bg-slate-900 border-b border-slate-800 shrink-0">
        <div className="flex items-center space-x-4">
          <div className="flex items-center justify-center w-10 h-10 bg-indigo-500/20 rounded-lg border border-indigo-500/30 text-indigo-400">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-white">LogSage AI</h1>
            <p className="text-xs text-slate-400 font-medium">Observability Platform</p>
          </div>
        </div>
        
        <MetricsBar logs={logs} />
      </header>

      {/* Main Content Area */}
      <div className="flex flex-1 overflow-hidden">
        
        {/* Left: Filters Sidebar */}
        <aside className="w-64 bg-slate-900/50 border-r border-slate-800 shrink-0 flex flex-col">
          <FiltersPanel 
            levelFilter={levelFilter}
            setLevelFilter={setLevelFilter}
            serviceFilter={serviceFilter}
            setServiceFilter={setServiceFilter}
            uniqueServices={uniqueServices}
          />
        </aside>

        {/* Center: Logs List */}
        <main className="flex-1 flex flex-col bg-slate-950 overflow-hidden relative">
          <div className="px-6 py-4 border-b border-slate-800/50 bg-slate-900/30 shrink-0 flex justify-between items-center">
            <h2 className="text-sm font-semibold text-slate-300">Live Log Stream</h2>
            <span className="text-xs text-slate-500">Showing {filteredAndSortedLogs.length} events</span>
          </div>
          
          <div className="flex-1 overflow-y-auto p-4 space-y-2">
            {loadingLogs && logs.length === 0 ? (
              <div className="flex justify-center items-center h-full text-slate-500 text-sm">
                <div className="animate-pulse">Loading stream...</div>
              </div>
            ) : (
              <LogsList 
                logs={filteredAndSortedLogs} 
                selectedLogId={selectedLog?.id} 
                onLogSelect={handleLogSelect} 
              />
            )}
          </div>
        </main>

        {/* Right: Analysis Panel */}
        <aside className="w-96 bg-slate-900 border-l border-slate-800 shrink-0 overflow-y-auto shadow-2xl">
          <AnalysisPanel 
            log={selectedLog} 
            analysis={analysis} 
            isLoading={loadingAnalysis} 
          />
        </aside>

      </div>
    </div>
  );
}
