import React from 'react';

const LEVEL_OPTIONS = [
  { value: '',      label: 'All Levels',  dot: 'bg-slate-500' },
  { value: 'ERROR', label: 'Error',       dot: 'bg-rose-500'  },
  { value: 'WARN',  label: 'Warning',     dot: 'bg-amber-400' },
  { value: 'INFO',  label: 'Info',        dot: 'bg-sky-500'   },
  { value: 'DEBUG', label: 'Debug',       dot: 'bg-slate-500' },
];

function SectionTitle({ children }) {
  return (
    <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500 mb-3">
      {children}
    </p>
  );
}

export default function FiltersPanel({
  levelFilter, setLevelFilter,
  serviceFilter, setServiceFilter,
  uniqueServices,
}) {
  return (
    <div className="p-4 flex flex-col gap-7">

      {/* Level Filter */}
      <div>
        <SectionTitle>Log Level</SectionTitle>
        <div className="flex flex-col gap-0.5">
          {LEVEL_OPTIONS.map(opt => {
            const active = levelFilter === opt.value;
            return (
              <button
                key={opt.value}
                onClick={() => setLevelFilter(opt.value)}
                className={`flex items-center gap-2.5 w-full px-2.5 py-2 rounded-md text-left transition-all duration-150 text-sm
                  ${active
                    ? 'bg-white/8 text-white font-medium'
                    : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
                  }`}
              >
                <span className={`w-2 h-2 rounded-full shrink-0 ${opt.dot} ${active ? 'ring-2 ring-white/20' : ''}`} />
                {opt.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Service Filter */}
      <div>
        <SectionTitle>Service</SectionTitle>
        <div className="flex flex-col gap-0.5">
          {[{ value: '', label: 'All Services' }, ...uniqueServices.map(s => ({ value: s, label: s }))].map(opt => {
            const active = serviceFilter === opt.value;
            return (
              <button
                key={opt.value}
                onClick={() => setServiceFilter(opt.value)}
                className={`flex items-center gap-2 w-full px-2.5 py-2 rounded-md text-left transition-all duration-150 text-sm truncate
                  ${active
                    ? 'bg-white/8 text-white font-medium'
                    : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
                  }`}
              >
                <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${active ? 'bg-violet-400' : 'bg-slate-600'}`} />
                <span className="truncate">{opt.label}</span>
              </button>
            );
          })}

          {uniqueServices.length === 0 && (
            <p className="text-xs text-slate-600 px-2.5">No services yet</p>
          )}
        </div>
      </div>

    </div>
  );
}
