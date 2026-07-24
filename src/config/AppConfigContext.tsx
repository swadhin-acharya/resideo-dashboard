import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

export interface AppConfig {
  projectName: string
  dashboardTitle: string
  dashboardSubtitle: string
  historyLimit: number
  reportRetention: number
}

const DEFAULT_CONFIG: AppConfig = {
  projectName: 'Automation Project',
  dashboardTitle: 'ResideoNextGen Dashboard',
  dashboardSubtitle: 'Automation Intelligence & Reporting',
  historyLimit: 50,
  reportRetention: 20,
}

const AppConfigContext = createContext<AppConfig>(DEFAULT_CONFIG)

export function useAppConfig() {
  return useContext(AppConfigContext)
}

// Resolves data URLs against the Vite base path so the app works when
// deployed under a GitHub Pages repository subpath (see vite.config.ts).
export function withBase(path: string): string {
  const base = import.meta.env.BASE_URL || '/'
  return `${base.replace(/\/$/, '')}/${path.replace(/^\//, '')}`
}

export function AppConfigProvider({ children }: { children: ReactNode }) {
  const [config, setConfig] = useState<AppConfig>(DEFAULT_CONFIG)

  useEffect(() => {
    let cancelled = false
    fetch(withBase('config.json'))
      .then((res) => (res.ok ? res.json() : Promise.reject(res.statusText)))
      .then((data) => {
        if (!cancelled) setConfig({ ...DEFAULT_CONFIG, ...data })
      })
      .catch(() => {
        // Fall back silently to defaults; the dashboard must still render
        // if a project has not shipped a config.json yet.
      })
    return () => {
      cancelled = true
    }
  }, [])

  return <AppConfigContext.Provider value={config}>{children}</AppConfigContext.Provider>
}
