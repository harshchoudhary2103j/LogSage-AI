import React from 'react';

export default function LogsTable({ logs, selectedLogId, onLogClick }) {
  if (!logs || logs.length === 0) {
    return <div className="empty-state">No logs found.</div>;
  }

  // Format timestamp nicely
  const formatTime = (ts) => {
    try {
      const date = new Date(ts);
      return date.toLocaleTimeString([], { hour12: false }) + '.' + String(date.getMilliseconds()).padStart(3, '0');
    } catch {
      return ts;
    }
  };

  return (
    <div className="table-container">
      <table className="logs-table">
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>Level</th>
            <th>Service</th>
            <th>Message</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr 
              key={log.id} 
              className={`log-row level-${log.level.toLowerCase()} ${selectedLogId === log.id ? 'selected' : ''}`}
              onClick={() => onLogClick(log)}
            >
              <td className="col-time">{formatTime(log.timestamp)}</td>
              <td className="col-level">
                <span className={`level-badge ${log.level.toLowerCase()}`}>{log.level}</span>
              </td>
              <td className="col-service">{log.service}</td>
              <td className="col-message" title={log.message}>{log.message}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
