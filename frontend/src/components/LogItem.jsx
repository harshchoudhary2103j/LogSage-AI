import React from 'react';

export default function LogItem({ log, isSelected, onClick }) {
  
  // Format timestamp nicely
  const formatTime = (ts) => {
    try {
      const date = new Date(ts);
      return date.toLocaleTimeString([], { hour12: false }) + '.' + String(date.getMilliseconds()).padStart(3, '0');
    } catch {
      return ts;
    }
  };

  // Border colors based on level
  const borderColors = {
    INFO: 'border-l-blue-500',
    WARN: 'border-l-amber-400',
    ERROR: 'border-l-rose-500',
    DEBUG: 'border-l-slate-500'
  };

  // Badge styles based on level
  const badgeStyles = {
    INFO: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
    WARN: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
    ERROR: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
    DEBUG: 'bg-slate-500/10 text-slate-400 border-slate-500/20'
  };

  const borderClass = borderColors[log.level] || 'border-l-slate-700';
  const badgeClass = badgeStyles[log.level] || 'bg-slate-800 text-slate-400 border-slate-700';

  return (
    <div 
      onClick={onClick}
      className={`group flex items-start p-3 bg-slate-900 border border-slate-800 border-l-4 rounded-md shadow-sm cursor-pointer transition-all duration-200 
        ${borderClass} 
        ${isSelected ? 'bg-slate-800 shadow-md transform scale-[1.01]' : 'hover:bg-slate-800/60'}`}
    >
      <div className="flex-1 min-w-0 pr-4">
        <div className="flex items-center space-x-3 mb-1">
          <span className={`text-[10px] font-bold px-2 py-0.5 rounded border uppercase tracking-widest ${badgeClass}`}>
            {log.level}
          </span>
          <span className="text-xs font-medium text-indigo-300">{log.service}</span>
          <span className="text-xs text-slate-500 font-mono">{formatTime(log.timestamp)}</span>
        </div>
        <p className="text-sm text-slate-300 font-mono truncate">{log.message}</p>
      </div>
      
      {/* Arrow indicator for ERROR logs */}
      {log.level === 'ERROR' && (
        <div className={`mt-2 text-rose-500 transition-transform ${isSelected ? 'translate-x-1 opacity-100' : 'opacity-0 group-hover:opacity-50'}`}>
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path></svg>
        </div>
      )}
    </div>
  );
}
