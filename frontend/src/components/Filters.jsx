import React from 'react';

export default function Filters({ currentFilter, onFilterChange }) {
  const levels = [
    { value: '', label: 'All Logs' },
    { value: 'ERROR', label: 'Errors Only' },
    { value: 'WARN', label: 'Warnings' },
    { value: 'INFO', label: 'Info' },
    { value: 'DEBUG', label: 'Debug' }
  ];

  return (
    <div className="filters-container">
      <div className="filter-group">
        <label htmlFor="level-filter">Filter Level:</label>
        <select 
          id="level-filter"
          value={currentFilter} 
          onChange={(e) => onFilterChange(e.target.value)}
          className="glass-select"
        >
          {levels.map(level => (
            <option key={level.value} value={level.value}>
              {level.label}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
