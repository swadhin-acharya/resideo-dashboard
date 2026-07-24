import { createContext, useContext, useState, useMemo, ReactNode } from 'react';
import { createTheme, Theme } from '@mui/material';

const DARK = {
  primary: { main: '#00A3E0', contrastText: '#fff' },
  secondary: { main: '#003B5C' },
  background: { default: '#0f1117', paper: '#1a1d27' },
  text: { primary: '#e1e4e8', secondary: '#8b949e' },
  divider: '#2d303a',
  success: { main: '#2da44e' },
  error: { main: '#da3633' },
  warning: { main: '#d29922' },
  info: { main: '#58a6ff' },
};

const LIGHT = {
  primary: { main: '#00A3E0', contrastText: '#fff' },
  secondary: { main: '#003B5C' },
  background: { default: '#f0f2f5', paper: '#ffffff' },
  text: { primary: '#1f2328', secondary: '#656d76' },
  divider: '#d0d7de',
  success: { main: '#1a7f37' },
  error: { main: '#cf222e' },
  warning: { main: '#9a6700' },
  info: { main: '#0969da' },
};

function buildTheme(mode: 'dark' | 'light') {
  const palette = mode === 'dark' ? DARK : LIGHT;
  return createTheme({
    palette: { mode, ...palette },
    typography: {
      fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      h4: { fontWeight: 700, fontSize: '1.5rem' },
      h5: { fontWeight: 600, fontSize: '1.25rem' },
      h6: { fontWeight: 600, fontSize: '1rem' },
      body2: { fontSize: '0.8125rem' },
      caption: { fontSize: '0.75rem' },
    },
    shape: { borderRadius: 10 },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: mode === 'dark'
              ? '0 1px 2px rgba(0,0,0,0.3)'
              : '0 1px 2px rgba(0,0,0,0.06)',
            border: `1px solid ${palette.divider}`,
            borderRadius: 10,
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            '&:last-child': { paddingBottom: 12 },
          },
        },
      },
      MuiButton: {
        styleOverrides: { root: { textTransform: 'none', fontWeight: 600, borderRadius: 8 } },
      },
      MuiChip: {
        styleOverrides: { root: { fontWeight: 500, borderRadius: 6 } },
      },
      MuiTableHead: {
        styleOverrides: {
          root: {
            '& .MuiTableCell-head': {
              fontWeight: 600,
              fontSize: '0.7rem',
              textTransform: 'uppercase',
              letterSpacing: 0.5,
              color: palette.text.secondary,
              borderBottom: `1px solid ${palette.divider}`,
            },
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            borderBottom: `1px solid ${palette.divider}`,
            fontSize: '0.8125rem',
          },
        },
      },
      MuiLinearProgress: {
        styleOverrides: {
          root: { borderRadius: 4, backgroundColor: palette.divider },
        },
      },
      MuiToolbar: {
        styleOverrides: {
          root: { minHeight: 56 },
        },
      },
      MuiDivider: {
        styleOverrides: {
          root: { borderColor: palette.divider },
        },
      },
    },
  });
}

interface ThemeCtx {
  mode: 'dark' | 'light';
  toggle: () => void;
  theme: Theme;
}

const ThemeContext = createContext<ThemeCtx>({ mode: 'dark', toggle: () => {}, theme: buildTheme('dark') });

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<'dark' | 'light'>('dark');
  const toggle = () => setMode(m => m === 'dark' ? 'light' : 'dark');
  const theme = useMemo(() => buildTheme(mode), [mode]);
  return <ThemeContext.Provider value={{ mode, toggle, theme }}>{children}</ThemeContext.Provider>;
}

export const useThemeMode = () => useContext(ThemeContext);
