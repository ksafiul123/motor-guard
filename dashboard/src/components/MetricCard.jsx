import React from 'react';

export default function MetricCard({ label, value, unit, warn, critical, icon }) {
  const num = parseFloat(value);
  let accent = '#1D9E75';
  let bg = '#e1f5ee';

  if (!isNaN(num)) {
    if (num >= critical) { accent = '#E24B4A'; bg = '#fcebeb'; }
    else if (num >= warn) { accent = '#EF9F27'; bg = '#faeeda'; }
  }

  const pct = !isNaN(num)
    ? Math.min(100, (num / critical) * 100)
    : 0;

  return (
    <div style={{
      background: '#fff',
      border: '1px solid #e8e8e8',
      borderRadius: 16,
      padding: '1.25rem 1.5rem',
      display: 'flex',
      flexDirection: 'column',
      gap: 12,
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: 12, fontWeight: 600, color: '#888',
          textTransform: 'uppercase', letterSpacing: '0.07em' }}>
          {label}
        </span>
        <span style={{ fontSize: 20 }}>{icon}</span>
      </div>

      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
        <span style={{ fontSize: 32, fontWeight: 700, color: '#1a1a1a', fontVariantNumeric: 'tabular-nums' }}>
          {value !== null && value !== undefined ? Number(value).toFixed(2) : '—'}
        </span>
        <span style={{ fontSize: 14, color: '#888' }}>{unit}</span>
      </div>

      {/* Progress bar */}
      <div style={{ background: '#f0f0f0', borderRadius: 99, height: 5, overflow: 'hidden' }}>
        <div style={{
          width: `${pct}%`,
          height: '100%',
          background: accent,
          borderRadius: 99,
          transition: 'width 0.6s ease, background 0.3s ease',
        }}/>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: '#bbb' }}>
        <span>0</span>
        <span style={{ color: '#EF9F27' }}>warn {warn}{unit}</span>
        <span style={{ color: '#E24B4A' }}>crit {critical}{unit}</span>
      </div>
    </div>
  );
}