import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout';
import DashboardPage from './pages/DashboardPage';
import ExecutionsPage from './pages/ExecutionsPage';
import NewExecutionPage from './pages/NewExecutionPage';
import ExecutionDetailPage from './pages/ExecutionDetailPage';
import AnalyticsPage from './pages/AnalyticsPage';
import DevicesPage from './pages/DevicesPage';
import ReportsPage from './pages/ReportsPage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/executions" element={<ExecutionsPage />} />
        <Route path="/executions/new" element={<NewExecutionPage />} />
        <Route path="/executions/:id" element={<ExecutionDetailPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
        <Route path="/devices" element={<DevicesPage />} />
      </Route>
    </Routes>
  );
}
