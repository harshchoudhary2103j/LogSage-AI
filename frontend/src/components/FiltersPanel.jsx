import React from 'react';

export default function FiltersPanel({ 
  levelFilter, setLevelFilter, 
  serviceFilter, setServiceFilter, 
  uniqueServices 
}) {
  
  const levels = [
    { value: '', label: 'All Levels' },
    { value: 'ERROR', label: 'Error', color: 'text-rose-500' },
    { value: 'WARN', label: 'Warning', color: 'text-amber-400' },
    { value: 'INFO', label: 'Info', color: 'text-blue-400' },
    { value: 'DEBUG', label: 'Debug', color: 'text-slate-400' }
  ];

  return (
    <div className="p-6 flex flex-col space-y-8">
      
      <div>
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">Log Level</h3>
        <div className="flex flex-col space-y-2">
          {levels.map(lvl => (
            <label 
              key={lvl.value} 
              className={`flex items-center p-2 rounded-md cursor-pointer transition-colors ${levelFilter === lvl.value ? 'bg-slate-800' : 'hover:bg-slate-800/50'}`}
            >
              <input 
                type="radio" 
                name="levelFilter" 
                value={lvl.value}
                checked={levelFilter === lvl.value}
                onChange={(e) => setLevelFilter(e.target.value)}
                className="hidden"
              />
              <div className={`w-3 h-3 rounded-full mr-3 border ${levelFilter === lvl.value ? 'border-4 border-slate-300' : 'border-slate-500'} ${lvl.value === '' ? 'bg-slate-400' : 'bg-current'} ${lvl.color || ''}`}></div>
              <span className={`text-sm ${levelFilter === lvl.value ? 'text-white font-medium' : 'text-slate-400'}`}>
                {lvl.label}
              </span>
            </label>
          ))}
        </div>
      </div>

      <div>
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">Service</h3>
        <select 
          value={serviceFilter}
          onChange={(e) => setServiceFilter(e.target.value)}
          className="w-full bg-slate-950 border border-slate-700 text-slate-300 text-sm rounded-md px-3 py-2 focus:outline-none focus:border-indigo-500 transition-colors"
        >
          <option value="">All Services</option>
          {uniqueServices.map(service => (
            <option key={service} value={service}>{service}</option>
          ))}
        </select>
      </div>

    </div>
  );
}
