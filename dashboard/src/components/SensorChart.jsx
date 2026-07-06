import React, { useEffect, useRef } from 'react';
import {
  Chart,
  LineController, LineElement, PointElement,
  LinearScale, CategoryScale,
  Tooltip, Legend, Filler,
} from 'chart.js';

Chart.register(
  LineController, LineElement, PointElement,
  LinearScale, CategoryScale,
  Tooltip, Legend, Filler
);

const DATASETS = [
  {
    key: 'temperature',
    label: 'Temperature (°C)',
    color: '#E24B4A',
    warnLine: 55,
  },
  {
    key: 'vibration',
    label: 'Vibration (m/s²)',
    color: '#EF9F27',
    warnLine: 2.0,
  },
  {
    key: 'current',
    label: 'Current (A)',
    color: '#378ADD',
    warnLine: 3.0,
  },
];

export default function SensorChart({ readings, activeMetric = 'temperature' }) {
  const canvasRef = useRef(null);
  const chartRef  = useRef(null);

  const ds = DATASETS.find(d => d.key === activeMetric) || DATASETS[0];

  useEffect(() => {
    if (!canvasRef.current) return;

    if (chartRef.current) chartRef.current.destroy();

    const labels = readings.map(r => {
      const d = new Date(r.timestamp);
      return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}:${d.getSeconds().toString().padStart(2,'0')}`;
    });

    const values = readings.map(r => parseFloat(r[ds.key]));

    chartRef.current = new Chart(canvasRef.current, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: ds.label,
          data: values,
          borderColor: ds.color,
          backgroundColor: ds.color + '18',
          borderWidth: 2,
          pointRadius: readings.length > 20 ? 0 : 3,
          pointHoverRadius: 5,
          fill: true,
          tension: 0.4,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 400 },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1a1a1a',
            titleColor: '#aaa',
            bodyColor: '#fff',
            padding: 10,
            cornerRadius: 8,
          },
        },
        scales: {
          x: {
            ticks: {
              color: '#aaa',
              font: { size: 11 },
              maxTicksLimit: 10,
              maxRotation: 0,
            },
            grid: { color: '#f0f0f0' },
          },
          y: {
            ticks: { color: '#aaa', font: { size: 11 } },
            grid: { color: '#f0f0f0' },
          },
        },
      },
      plugins: [{
        // Draw warning line
        id: 'warnLine',
        afterDraw(chart) {
          const { ctx, chartArea: { left, right }, scales: { y } } = chart;
          if (!y) return;
          const yPx = y.getPixelForValue(ds.warnLine);
          ctx.save();
          ctx.setLineDash([6, 4]);
          ctx.strokeStyle = '#EF9F2766';
          ctx.lineWidth = 1.5;
          ctx.beginPath();
          ctx.moveTo(left, yPx);
          ctx.lineTo(right, yPx);
          ctx.stroke();
          ctx.restore();
        },
      }],
    });

    return () => { if (chartRef.current) chartRef.current.destroy(); };
  }, [readings, activeMetric]);

  return <canvas ref={canvasRef} style={{ width: '100%', height: '100%' }}/>;
}