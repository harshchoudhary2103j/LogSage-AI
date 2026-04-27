import React, { useState } from 'react';
import Header from './components/Header.jsx';
import LogInput from './components/LogInput.jsx';
import AnalysisResult from './components/AnalysisResult.jsx';
import { analyzeLogs, submitLogs } from './services/api.js';

export default function App() {
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAnalyze = async (logs) => {
    setLoading(true);
    setError('');
    setResult(null);

    try {
      // Step 1: Store logs
      await submitLogs(logs);

      // Step 2: Analyze with AI
      const analysis = await analyzeLogs(logs);
      setResult(analysis);
    } catch (err) {
      setError(err.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <div className="background-effects">
        <div className="bg-orb bg-orb-1"></div>
        <div className="bg-orb bg-orb-2"></div>
        <div className="bg-orb bg-orb-3"></div>
      </div>
      <Header />
      <main className="main-content">
        <LogInput onAnalyze={handleAnalyze} isLoading={loading} />
        <AnalysisResult result={result} error={error} />
      </main>
      <footer className="footer">
        <p>LogSage AI — Phase 1 MVP &middot; Built with Spring Boot + React</p>
      </footer>
    </div>
  );
}
