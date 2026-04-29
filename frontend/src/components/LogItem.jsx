import React from 'react';

const LEVEL_STYLES = {
  INFO:  { border: 'border-l-sky-500',   badge: 'text-sky-400   bg-sky-500/10   border-sky-500/20'   },
  WARN:  { border: 'border-l-amber-400', badge: 'text-amber-400 bg-amber-400/10 border-amber-400/20' },
  ERROR: { border: 'border-l-rose-500',  badge: 'text-rose-400  bg-rose-500/10  border-rose-500/20'  },
  DEBUG: { border: 'border-l-slate-500', badge: 'text-slate-400 bg-slate-500/10 border-slate-500/20' },
};

function formatTs(ts) {
  try {
    const d = new Date(ts);
    const hms = d.toLocaleTimeString([], { hour12: false });
    const ms  = String(d.getMilliseconds()).padStart(3, '0');
    return `${hms}.${ms}`;
  } catch { return ts; }
}

export default function LogItem({ log, isSelected, onClick }) {
  const styles = LEVEL_STYLES[log.level] ?? LEVEL_STYLES.DEBUG;

  return (
    <div
      onClick={onClick}
      className={`
        group relative flex items-start gap-3 px-4 py-3
        border-l-2 rounded-r-md cursor-pointer
        transition-all duration-150 ease-out
        ${styles.border}
        ${isSelected
          ? 'bg-white/6 shadow-sm'
          : 'bg-[#111827]/60 hover:bg-white/4'
        }
      `}
    >
      {/* Left: badge + service + time */}
      <div className="flex flex-col gap-1 shrink-0 min-w-[80px]">
        <span className={`self-start inline-flex items-center px-1.5 py-0.5 rounded text-[9px] font-bold tracking-widest uppercase border ${styles.badge}`}>
          {log.level}
        </span>
        <span className="text-[10px] text-slate-500 font-mono">{formatTs(log.timestamp)}</span>
      </div>

      {/* Right: message + service name */}
      <div className="flex-1 min-w-0">
        <p className="text-sm font-mono text-slate-200 leading-snug truncate">
          {log.message}
        </p>
        <p className="text-[11px] text-slate-500 mt-0.5 truncate">
          {log.service}
        </p>
      </div>

      {/* Arrow hint on ERROR (only visible on selected/hover) */}
      {log.level === 'ERROR' && (
        <div className={`shrink-0 mt-1 transition-opacity duration-150 text-rose-500
          ${isSelected ? 'opacity-100' : 'opacity-0 group-hover:opacity-40'}`}>
          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
          </svg>
        </div>
      )}
    </div>
  );
}
