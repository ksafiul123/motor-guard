import React from 'react';
import { resolveAlert } from '../api/api';

const TYPE_ICON = {
  OVERHEAT:  '🌡',
  VIBRATION: '📳',
  OVERLOAD:  '⚡',
};

export default function AlertPanel({ alerts, onResolved }) {
  const active = alerts.filter(a => !a.resolved);

  async function handleResolve(id) {
    try {
      await resolveAlert(id);
      onResolved();
    } catch (e) {
      console.error(e);
    }
  }

  if (active.length === 0) {
    return (
      <div style={{
        textAlign: 'center', padding: '2rem',
        color: '#1D9E75', fontSize: 14,
      }}>
        ✅ No active alerts
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {active.map(alert => (
        <div key={alert.id} style={{
          background: alert.severity === 'CRITICAL' ? '#fcebeb' : '#faeeda',
          border: `1px solid ${alert.severity === 'CRITICAL' ? '#E24B4A44' : '#EF9F2744'}`,
          borderLeft: `4px solid ${alert.severity === 'CRITICAL' ? '#E24B4A' : '#EF9F27'}`,
          borderRadius: 10,
          padding: '0.9rem 1rem',
          display: 'flex',
          gap: 12,
          alignItems: 'flex-start',
        }}>
          <span style={{ fontSize: 20, flexShrink: 0, marginTop: 1 }}>
            {TYPE_ICON[alert.type] || '⚠️'}
          </span>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between',
              alignItems: 'center', marginBottom: 4 }}>
              <span style={{
                fontSize: 11, fontWeight: 700,
                color: alert.severity === 'CRITICAL' ? '#791F1F' : '#633806',
                textTransform: 'uppercase', letterSpacing: '0.07em',
              }}>
                {alert.severity} · {alert.type}
              </span>
              <span style={{ fontSize: 11, color: '#999' }}>
                {new Date(alert.timestamp).toLocaleTimeString()}
              </span>
            </div>
            <div style={{ fontSize: 13, color: '#333', lineHeight: 1.5, marginBottom: 8 }}>
              {alert.message}
            </div>
            <button
              onClick={() => handleResolve(alert.id)}
              style={{
                fontSize: 12, padding: '4px 12px',
                background: 'transparent',
                border: `1px solid ${alert.severity === 'CRITICAL' ? '#E24B4A' : '#EF9F27'}`,
                borderRadius: 6,
                color: alert.severity === 'CRITICAL' ? '#791F1F' : '#633806',
                cursor: 'pointer',
                fontWeight: 500,
              }}>
              Resolve
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}