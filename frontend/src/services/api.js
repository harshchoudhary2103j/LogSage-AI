const API_BASE_URL = 'http://localhost:8081/api';

/**
 * Fetch all logs, optionally filtered by level.
 * @param {string} level - Optional log level (INFO, WARN, ERROR, DEBUG)
 * @returns {Promise<Array>} List of log entries
 */
export async function fetchLogs(level = '') {
    const url = level ? `${API_BASE_URL}/logs?level=${level}` : `${API_BASE_URL}/logs`;
    
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Failed to fetch logs: ${response.statusText}`);
    }
    
    return response.json();
}

/**
 * Fetch AI analysis for a specific log ID.
 * @param {number} logId - The ID of the log entry
 * @returns {Promise<Object>} Analysis result
 */
export async function fetchAnalysis(logId) {
    const response = await fetch(`${API_BASE_URL}/analysis/${logId}`);
    
    if (response.status === 404) {
        return null; // Analysis might be pending or not exist
    }
    
    if (!response.ok) {
        throw new Error(`Failed to fetch analysis: ${response.statusText}`);
    }
    
    return response.json();
}
