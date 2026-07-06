import React, { useState } from 'react';
import StatusCard  from './components/StatusCard';
import MetricCard  from './components/MetricCard';
import SensorChart from './components/SensorChart';
import AlertPanel  from './components/AlertPanel';
import { useMotorStatus, useSensorData, useAlerts } from './hooks/useData';

// ── Change this to your motor's ID (from POST /api/motors response) ──────────
const MOTOR_ID = 1;

const METRICS = [
  { key: 'temperature', label: 'Temperature', unit: '°C',   warn: 55,  critical: 70,  icon: '🌡️' },
  { key: 'vibration',   label: 'Vibration',   unit: ' m/s²', warn: 2.0, critical: 5.0, icon: '📳' },
  { key: 'current',     label: 'Current',     unit: 'A',    warn: 3.0, critical: 4.5, icon: '⚡' },
];

export default function App() {
  const [activeMetric, setActiveMetric] = useState('temperature');

  const { status,   loading: sLoading }           = useMotorStatus(MOTOR_ID);
  const { readings, loading: rLoading, refetch }  = useSensorData(MOTOR_ID, 60);
  const { alerts,   loading: aLoading, refetch: refetchAlerts } = useAlerts();

  const latest = readings[readings.length - 1];
  const isConnected = readings.length > 0;

  return (
    <div style={{
      minHeight: '100vh',
      background: '#f5f5f3',
      fontFamily: '"DM Sans", system-ui, sans-serif',
    }}>
      {/* Google Font */}
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=DM+Mono:wght@400;500&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        @keyframes ping {
          75%, 100% { transform: scale(2); opacity: 0; }
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(8px); }
          to   { opacity: 1; transform: translateY(0); }
        }
        .card { animation: fadeIn 0.4s ease both; }
        .metric-tab { transition: all 0.2s; }
        .metric-tab:hover { opacity: 0.8; }
      `}</style>

      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <header style={{
        background: '#fff',
        borderBottom: '1px solid #e8e8e8',
        padding: '1rem 2rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 100,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 36, height: 36,
            background: '#1a1a1a',
            borderRadius: 10,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 18,
          }}>⚙️</div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 700, color: '#1a1a1a' }}>
              PredictiveMaint
            </div>
            <div style={{ fontSize: 11, color: '#999' }}>
              Motor Health Dashboard
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          {/* Connection indicator */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12,
            color: isConnected ? '#085041' : '#791F1F' }}>
            <span style={{
              width: 8, height: 8, borderRadius: '50%',
              background: isConnected ? '#1D9E75' : '#E24B4A',
              display: 'inline-block',
            }}/>
            {isConnected ? 'Connected' : 'No data'}
          </div>

          {/* Active alert badge */}
          {alerts.filter(a => !a.resolved).length > 0 && (
            <div style={{
              background: '#E24B4A', color: '#fff',
              borderRadius: 20, padding: '3px 10px',
              fontSize: 12, fontWeight: 600,
            }}>
              🔔 {alerts.filter(a => !a.resolved).length} alert{alerts.filter(a => !a.resolved).length > 1 ? 's' : ''}
            </div>
          )}

          {/* Last update */}
          <div style={{ fontSize: 11, color: '#bbb', fontFamily: '"DM Mono", monospace' }}>
            {latest ? `Updated ${new Date(latest.timestamp).toLocaleTimeString()}` : 'Waiting...'}
          </div>
        </div>
      </header>

      {/* ── Main content ───────────────────────────────────────────────────── */}
      <main style={{ maxWidth: 1280, margin: '0 auto', padding: '2rem' }}>

        {/* Row 1: Status + Metrics */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr 1fr 1fr',
          gap: 16,
          marginBottom: 16,
        }}>
          {/* Status card spans 1 column */}
          <div className="card" style={{ animationDelay: '0ms' }}>
            {sLoading || !status ? (
              <Skeleton height={120}/>
            ) : (
              <StatusCard
                status={status.status}
                name={status.name}
                location={status.location}
                activeAlerts={status.activeAlertCount}
              />
            )}
          </div>

          {/* 3 metric cards */}
          {METRICS.map((m, i) => (
            <div key={m.key} className="card" style={{ animationDelay: `${(i+1)*60}ms` }}>
              {rLoading || !latest ? (
                <Skeleton height={120}/>
              ) : (
                <MetricCard
                  label={m.label}
                  value={latest[m.key]}
                  unit={m.unit}
                  warn={m.warn}
                  critical={m.critical}
                  icon={m.icon}
                />
              )}
            </div>
          ))}
        </div>

        {/* Row 2: Chart + Alerts */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '2fr 1fr',
          gap: 16,
        }}>

          {/* Chart panel */}
          <div className="card" style={{
            background: '#fff',
            border: '1px solid #e8e8e8',
            borderRadius: 16,
            padding: '1.5rem',
            animationDelay: '240ms',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between',
              alignItems: 'center', marginBottom: 16 }}>
              <div style={{ fontSize: 15, fontWeight: 700, color: '#1a1a1a' }}>
                Sensor Trend
              </div>

              {/* Metric selector tabs */}
              <div style={{
                display: 'flex', background: '#f5f5f3',
                borderRadius: 8, padding: 3, gap: 2,
              }}>
                {METRICS.map(m => (
                  <button
                    key={m.key}
                    className="metric-tab"
                    onClick={() => setActiveMetric(m.key)}
                    style={{
                      padding: '5px 12px',
                      fontSize: 12, fontWeight: 500,
                      border: 'none', borderRadius: 6, cursor: 'pointer',
                      background: activeMetric === m.key ? '#fff' : 'transparent',
                      color: activeMetric === m.key ? '#1a1a1a' : '#888',
                      boxShadow: activeMetric === m.key ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                    }}>
                    {m.icon} {m.label}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ height: 280 }}>
              {rLoading ? (
                <Skeleton height={280}/>
              ) : readings.length === 0 ? (
                <div style={{ display: 'flex', alignItems: 'center',
                  justifyContent: 'center', height: '100%',
                  color: '#bbb', fontSize: 14 }}>
                  Waiting for sensor data...
                </div>
              ) : (
                <SensorChart readings={readings} activeMetric={activeMetric}/>
              )}
            </div>

            {/* Reading count */}
            <div style={{ fontSize: 11, color: '#bbb', marginTop: 10,
              fontFamily: '"DM Mono", monospace' }}>
              {readings.length} readings · polling every 5s
            </div>
          </div>

          {/* Alert panel */}
          <div className="card" style={{
            background: '#fff',
            border: '1px solid #e8e8e8',
            borderRadius: 16,
            padding: '1.5rem',
            animationDelay: '300ms',
            overflow: 'hidden',
          }}>
            <div style={{ fontSize: 15, fontWeight: 700, color: '#1a1a1a', marginBottom: 16,
              display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>Active Alerts</span>
              {alerts.filter(a => !a.resolved).length > 0 && (
                <span style={{
                  background: '#E24B4A', color: '#fff',
                  borderRadius: 50, width: 22, height: 22,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 11, fontWeight: 700,
                }}>
                  {alerts.filter(a => !a.resolved).length}
                </span>
              )}
            </div>
            <div style={{ overflowY: 'auto', maxHeight: 320 }}>
              {aLoading ? (
                <Skeleton height={100}/>
              ) : (
                <AlertPanel
                  alerts={alerts}
                  onResolved={refetchAlerts}
                />
              )}
            </div>
          </div>
        </div>

        {/* Row 3: Raw readings table */}
        <div className="card" style={{
          background: '#fff',
          border: '1px solid #e8e8e8',
          borderRadius: 16,
          padding: '1.5rem',
          marginTop: 16,
          animationDelay: '360ms',
        }}>
          <div style={{ fontSize: 15, fontWeight: 700, color: '#1a1a1a', marginBottom: 16 }}>
            Recent Readings
          </div>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
              <thead>
                <tr style={{ borderBottom: '1px solid #f0f0f0' }}>
                  {['Time', 'Temperature', 'Vibration', 'Current', 'Status'].map(h => (
                    <th key={h} style={{
                      textAlign: 'left', padding: '8px 12px',
                      fontSize: 11, fontWeight: 600, color: '#999',
                      textTransform: 'uppercase', letterSpacing: '0.06em',
                    }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {[...readings].reverse().slice(0, 10).map((r, i) => (
                  <tr key={r.id || i} style={{
                    borderBottom: '1px solid #f8f8f8',
                    background: i % 2 === 0 ? '#fff' : '#fafafa',
                  }}>
                    <td style={{ padding: '9px 12px', fontFamily: '"DM Mono", monospace',
                      color: '#666', fontSize: 12 }}>
                      {new Date(r.timestamp).toLocaleTimeString()}
                    </td>
                    <td style={{ padding: '9px 12px', color: parseFloat(r.temperature) >= 55 ? '#E24B4A' : '#1a1a1a' }}>
                      {Number(r.temperature).toFixed(2)} °C
                    </td>
                    <td style={{ padding: '9px 12px', color: parseFloat(r.vibration) >= 2 ? '#EF9F27' : '#1a1a1a' }}>
                      {Number(r.vibration).toFixed(4)} m/s²
                    </td>
                    <td style={{ padding: '9px 12px', color: parseFloat(r.current) >= 3 ? '#EF9F27' : '#1a1a1a' }}>
                      {Number(r.current).toFixed(3)} A
                    </td>
                    <td style={{ padding: '9px 12px' }}>
                      <span style={{
                        padding: '3px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600,
                        background: r.status === 'CRITICAL' ? '#fcebeb'
                          : r.status === 'WARNING' ? '#faeeda' : '#e1f5ee',
                        color: r.status === 'CRITICAL' ? '#791F1F'
                          : r.status === 'WARNING' ? '#633806' : '#085041',
                      }}>
                        {r.status}
                      </span>
                    </td>
                  </tr>
                ))}
                {readings.length === 0 && (
                  <tr><td colSpan={5} style={{ padding: '2rem', textAlign: 'center',
                    color: '#bbb', fontSize: 13 }}>
                    No readings yet — waiting for ESP32...
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

      </main>
    </div>
  );
}

// Skeleton loader
function Skeleton({ height }) {
  return (
    <div style={{
      height,
      background: 'linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%)',
      backgroundSize: '200% 100%',
      borderRadius: 12,
      animation: 'shimmer 1.5s infinite',
    }}/>
  );
}