import React from 'react';
import LogItem from './LogItem';

export default function LogsList({ logs, selectedLogId, onLogSelect }) {
  if (!logs || logs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-slate-500">
        <svg className="w-16 h-16 mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
        <p>No logs found matching your criteria.</p>
      </div>
    );
  }

  return (
    <>
      {logs.map((log) => (
        <LogItem 
          key={log.id} 
          log={log} 
          isSelected={selectedLogId === log.id}
          onClick={() => onLogSelect(log)}
        />
      ))}
    </>
  );
}
