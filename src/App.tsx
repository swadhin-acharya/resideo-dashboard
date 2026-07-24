import { HashRouter, Routes, Route } from 'react-router-dom'
import { AppThemeProvider } from './theme/ThemeModeContext'
import { AppConfigProvider } from './config/AppConfigContext'
import { AppShell } from './components/layout/AppShell'
import OverviewPage from './pages/Overview'

// HashRouter is used deliberately: GitHub Pages serves static files with no
// server-side rewrite rules, so a browser-history router would 404 on a
// deep-link refresh (e.g. /resideo-nextgen-dashboard/executions/225). Hash
// routing keeps all navigation client-side and works reliably under any
// GitHub Pages repository subpath.
function App() {
  return (
    <AppConfigProvider>
      <AppThemeProvider>
        <HashRouter>
          <Routes>
            <Route element={<AppShell />}>
              <Route path="/" element={<OverviewPage />} />
            </Route>
          </Routes>
        </HashRouter>
      </AppThemeProvider>
    </AppConfigProvider>
  )
}

export default App
