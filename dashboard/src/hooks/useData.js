import { useState, useEffect, useCallback } from 'react';
import { fetchMotorStatus, fetchSensorData, fetchAlerts, fetchAllMotors } from '../api/api';

// Polls motor status every 5 seconds
export function useMotorStatus(motorId) {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]   = useState(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchMotorStatus(motorId);
      setStatus(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [motorId]);

  useEffect(() => {
    load();
    const interval = setInterval(load, 5000);
    return () => clearInterval(interval);
  }, [load]);

  return { status, loading, error, refetch: load };
}

// Polls sensor readings every 5 seconds
export function useSensorData(motorId, limit = 50) {
  const [readings, setReadings] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchSensorData(motorId, limit);
      setReadings(data.reverse()); // oldest → newest for chart
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [motorId, limit]);

  useEffect(() => {
    load();
    const interval = setInterval(load, 5000);
    return () => clearInterval(interval);
  }, [load]);

  return { readings, loading, error, refetch: load };
}

// Polls active alerts every 5 seconds
export function useAlerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchAlerts();
      setAlerts(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, 5000);
    return () => clearInterval(interval);
  }, [load]);

  return { alerts, loading, error, refetch: load };
}

// Fetches all motors once
export function useMotors() {
  const [motors, setMotors]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);

  useEffect(() => {
    fetchAllMotors()
      .then(setMotors)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { motors, loading, error };
}