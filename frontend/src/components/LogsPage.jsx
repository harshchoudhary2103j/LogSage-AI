import React, { useState, useEffect } from 'react';
import { fetchLogs, fetchAnalysis } from '../services/api';
import LogsTable from './LogsTable';
import Filters from './Filters';
import AnalysisPanel from './AnalysisPanel';

export default function LogsPage() {
  const [logs, setLogs] = useState([]);
  const [loadingLogs, setLoadingLogs] = useState(true);
  const [filterLevel, setFilterLevel] = useState('');
  
  const [selectedLog, setSelectedLog] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);

  // Poll for logs every 5 seconds
  useEffect(() => {
    loadLogs();
    const interval = setInterval(loadLogs, 5000);
    return () => clearInterval(interval);
  }, [filterLevel]);

  const loadLogs = async () => {
    try {
      const data = await fetchLogs(filterLevel);
      setLogs(data);
    } catch (error) {
      console.error('Error fetching logs:', error);
    } finally {
      setLoadingLogs(false);
    }
  };

  const handleLogClick = async (log) => {
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

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="logo-section">
          <h1>LogSage AI</h1>
          <span className="badge">Observability Dashboard</span>
        </div>
        <Filters currentFilter={filterLevel} onFilterChange={setFilterLevel} />
      </header>

      <div className="dashboard-content">
        <div className="logs-section">
          {loadingLogs && logs.length === 0 ? (
            <div className="loading-state">Loading logs...</div>
          ) : (
            <LogsTable 
              logs={logs} 
              selectedLogId={selectedLog?.id} 
              onLogClick={handleLogClick} 
            />
          )}
        </div>
        
        <div className="analysis-section">
          <AnalysisPanel 
            log={selectedLog} 
            analysis={analysis} 
            isLoading={loadingAnalysis} 
          />
        </div>
      </div>
    </div>
  );
}
