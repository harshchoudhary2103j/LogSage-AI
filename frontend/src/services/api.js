const API_BASE = '/api';

/**
 * Submit log entries to the backend for storage.
 */
export async function submitLogs(logs) {
  const response = await fetch(`${API_BASE}/logs`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(logs),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to submit logs' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Send logs to the AI analysis endpoint.
 */
export async function analyzeLogs(logs) {
  const response = await fetch(`${API_BASE}/analyze`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logs }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Analysis failed' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  return response.json();
}
