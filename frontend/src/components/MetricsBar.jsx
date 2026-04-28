import React, { useMemo } from 'react';

export default function MetricsBar({ logs }) {
  
  const metrics = useMemo(() => {
    let errors = 0;
    let warnings = 0;
    
    logs.forEach(l => {
      if (l.level === 'ERROR') errors++;
      if (l.level === 'WARN') warnings++;
    });
    
    return {
      total: logs.length,
      errors,
      warnings
    };
  }, [logs]);

  return (
    <div className="flex items-center space-x-6">
      
      <div className="flex flex-col items-end">
        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Logs</span>
        <span className="text-xl font-bold text-slate-200">{metrics.total}</span>
      </div>
      
      <div className="h-8 w-px bg-slate-700"></div>

      <div className="flex flex-col items-end">
        <span className="text-xs font-semibold text-amber-500/80 uppercase tracking-wider">Warnings</span>
        <span className="text-xl font-bold text-amber-400">{metrics.warnings}</span>
      </div>
      
      <div className="h-8 w-px bg-slate-700"></div>

      <div className="flex flex-col items-end">
        <span className="text-xs font-semibold text-rose-500/80 uppercase tracking-wider">Errors</span>
        <span className="text-xl font-bold text-rose-500">{metrics.errors}</span>
      </div>

    </div>
  );
}
