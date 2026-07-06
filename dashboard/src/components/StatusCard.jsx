import React from 'react';

const STATUS_STYLES = {
  NORMAL:   { bg: '#e1f5ee', color: '#085041', dot: '#1D9E75', label: 'Normal'   },
  WARNING:  { bg: '#faeeda', color: '#633806', dot: '#EF9F27', label: 'Warning'  },
  CRITICAL: { bg: '#fcebeb', color: '#791F1F', dot: '#E24B4A', label: 'Critical' },
};

export default function StatusCard({ status, name, location, activeAlerts }) {
  const style = STATUS_STYLES[status] || STATUS_STYLES.NORMAL;

  return (
    <div style={{
      background: style.bg,
      border: `1px solid ${style.dot}33`,
      borderRadius: 16,
      padding: '1.5rem',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '1rem',
    }}>
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
          {/* Pulsing dot */}
          <span style={{ position: 'relative', display: 'inline-flex' }}>
            <span style={{
              width: 10, height: 10, borderRadius: '50%',
              background: style.dot, display: 'block',
            }}/>
            {status !== 'NORMAL' && (
              <span style={{
                position: 'absolute', top: 0, left: 0,
                width: 10, height: 10, borderRadius: '50%',
                background: style.dot, opacity: 0.4,
                animation: 'ping 1.5s cubic-bezier(0,0,0.2,1) infinite',
              }}/>
            )}
          </span>
          <span style={{ fontSize: 13, fontWeight: 600, color: style.color,
            textTransform: 'uppercase', letterSpacing: '0.08em' }}>
            {style.label}
          </span>
        </div>
        <div style={{ fontSize: 22, fontWeight: 700, color: style.color, marginBottom: 2 }}>
          {name}
        </div>
        <div style={{ fontSize: 13, color: style.color, opacity: 0.7 }}>
          {location}
        </div>
      </div>
      {activeAlerts > 0 && (
        <div style={{
          background: '#E24B4A', color: '#fff',
          borderRadius: 50, width: 36, height: 36,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 14, fontWeight: 700, flexShrink: 0,
        }}>
          {activeAlerts}
        </div>
      )}
    </div>
  );
}