import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout';
import DashboardPage from './pages/DashboardPage';
import ExecutionsPage from './pages/ExecutionsPage';
import NewExecutionPage from './pages/NewExecutionPage';
import ExecutionDetailPage from './pages/ExecutionDetailPage';
import AnalyticsPage from './pages/AnalyticsPage';
import DevicesPage from './pages/DevicesPage';
import ThermostatsPage from './pages/ThermostatsPage';
import SchedulesPage from './pages/SchedulesPage';
import LoginPage from './pages/LoginPage';
import UsersPage from './pages/UsersPage';
import SettingsPage from './pages/SettingsPage';
import ReportsPage from './pages/ReportsPage';
import PlaceholderPage from './pages/placeholders/PlaceholderPage';
import PrivateRoute from './components/auth/PrivateRoute';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<PrivateRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/executions" element={<ExecutionsPage />} />
          <Route path="/executions/new" element={<NewExecutionPage />} />
          <Route path="/executions/:id" element={<ExecutionDetailPage />} />
          <Route path="/live" element={<PlaceholderPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/flaky-tests" element={<PlaceholderPage />} />
          <Route path="/test-explorer" element={<PlaceholderPage />} />
          <Route path="/devices" element={<DevicesPage />} />
          <Route path="/thermostats" element={<ThermostatsPage />} />
          <Route path="/mqtt" element={<PlaceholderPage />} />
          <Route path="/serial" element={<PlaceholderPage />} />
          <Route path="/environments" element={<PlaceholderPage />} />
          <Route path="/configurations" element={<PlaceholderPage />} />
          <Route path="/schedules" element={<SchedulesPage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/integrations" element={<PlaceholderPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
