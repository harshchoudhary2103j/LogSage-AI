import React from 'react';
import LogItem from './LogItem';

export default function LogsList({ logs, selectedLogId, onLogSelect }) {
  if (!logs || logs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-slate-600 gap-3 py-20">
        <svg className="w-10 h-10 opacity-30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p className="text-sm">No logs match the current filters</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-1">
      {logs.map(log => (
        <LogItem
          key={log.id}
          log={log}
          isSelected={selectedLogId === log.id}
          onClick={() => onLogSelect(log)}
        />
      ))}
    </div>
  );
}
