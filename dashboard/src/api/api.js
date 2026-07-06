const BASE_URL = 'http://localhost:8080/api';

export async function fetchMotorStatus(motorId) {
  const res = await fetch(`${BASE_URL}/motors/status/${motorId}`);
  if (!res.ok) throw new Error('Failed to fetch motor status');
  return res.json();
}

export async function fetchSensorData(motorId, limit = 50) {
  const res = await fetch(`${BASE_URL}/data/${motorId}?limit=${limit}`);
  if (!res.ok) throw new Error('Failed to fetch sensor data');
  return res.json();
}

export async function fetchAlerts() {
  const res = await fetch(`${BASE_URL}/alerts`);
  if (!res.ok) throw new Error('Failed to fetch alerts');
  return res.json();
}

export async function fetchMotorAlerts(motorId) {
  const res = await fetch(`${BASE_URL}/alerts/${motorId}`);
  if (!res.ok) throw new Error('Failed to fetch motor alerts');
  return res.json();
}

export async function resolveAlert(alertId) {
  const res = await fetch(`${BASE_URL}/alerts/${alertId}/resolve`, {
    method: 'PATCH',
  });
  if (!res.ok) throw new Error('Failed to resolve alert');
  return res.json();
}

export async function fetchAllMotors() {
  const res = await fetch(`${BASE_URL}/motors`);
  if (!res.ok) throw new Error('Failed to fetch motors');
  return res.json();
}