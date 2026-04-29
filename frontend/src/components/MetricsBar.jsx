import React, { useMemo } from 'react';

function StatCard({ label, value, color }) {
  return (
    <div className="flex items-center gap-3">
      <div className="flex flex-col">
        <span className={`text-xl font-bold tabular-nums leading-none ${color}`}>{value}</span>
        <span className="text-[10px] font-semibold uppercase tracking-widest text-slate-500 mt-0.5">{label}</span>
      </div>
    </div>
  );
}

export default function MetricsBar({ logs }) {
  const { total, errors, warnings, info } = useMemo(() => {
    let errors = 0, warnings = 0, info = 0;
    logs.forEach(l => {
      if (l.level === 'ERROR') errors++;
      else if (l.level === 'WARN') warnings++;
      else if (l.level === 'INFO') info++;
    });
    return { total: logs.length, errors, warnings, info };
  }, [logs]);

  const sep = <div className="h-6 w-px bg-white/8" />;

  return (
    <div className="flex items-center gap-6">
      <StatCard label="Total"    value={total}    color="text-slate-200" />
      {sep}
      <StatCard label="Errors"   value={errors}   color="text-rose-400"  />
      {sep}
      <StatCard label="Warnings" value={warnings} color="text-amber-400" />
      {sep}
      <StatCard label="Info"     value={info}     color="text-sky-400"   />
    </div>
  );
}
