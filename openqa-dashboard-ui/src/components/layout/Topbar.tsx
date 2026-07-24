import { useCallback, useEffect, useState } from 'react';
import { useSearchParams, useLocation, useNavigate } from 'react-router-dom';
import {
  AppBar, Toolbar, IconButton, Typography, Box, InputBase, alpha,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { useThemeMode } from '../../context/ThemeContext';

interface TopbarProps {
  open: boolean;
  onToggle: () => void;
}

const SEARCH_PAGES = ['/dashboard', '/executions'];

export default function Topbar({ open, onToggle }: TopbarProps) {
  const { mode, toggle } = useThemeMode();
  const [searchParams, setSearchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [query, setQuery] = useState(searchParams.get('q') || '');

  const showSearch = SEARCH_PAGES.some(p => location.pathname === p || location.pathname.startsWith(p + '/'));

  useEffect(() => {
    if (!showSearch) setQuery('');
  }, [showSearch]);

  const handleSearch = useCallback((value: string) => {
    setQuery(value);
    if (value) {
      searchParams.set('q', value);
    } else {
      searchParams.delete('q');
    }
    setSearchParams(searchParams, { replace: true });
  }, [searchParams, setSearchParams]);

  return (
    <AppBar
      position="fixed" elevation={0}
      sx={{
        bgcolor: 'background.paper', borderBottom: '1px solid', borderColor: 'divider',
        zIndex: (t) => t.zIndex.drawer + 1,
      }}
    >
      <Toolbar sx={{ gap: 2, minHeight: { xs: 56, sm: 56, md: 56 }, height: { xs: 56, sm: 56, md: 56 } }}>
        <IconButton edge="start" onClick={onToggle} sx={{ color: 'text.secondary' }}>
          <MenuIcon />
        </IconButton>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mr: 1 }}>
          <img src="/assets/openqa-logo.png" alt="OpenQA" height={36} />
          <Typography variant="h6" fontWeight={700} color="text.primary" sx={{ fontSize: '1.05rem', letterSpacing: '-0.3px', whiteSpace: 'nowrap' }}>
            OpenQA
          </Typography>
        </Box>

        {showSearch && (
          <Box sx={{ flex: 1, maxWidth: 420, display: { xs: 'none', md: 'flex' }, alignItems: 'center', bgcolor: (t) => alpha(t.palette.text.secondary, 0.08), borderRadius: 1.5, px: 1.5 }}>
            <SearchIcon sx={{ color: 'text.secondary', fontSize: 18, mr: 1 }} />
            <InputBase
              placeholder="Search executions..."
              value={query}
              onChange={(e) => handleSearch(e.target.value)}
              sx={{ flex: 1, fontSize: '0.8125rem', py: 0.625, color: 'text.primary' }}
            />
          </Box>
        )}

        <Box sx={{ flex: 1 }} />

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <IconButton onClick={toggle} sx={{ color: 'text.secondary' }} size="small">
            {mode === 'dark' ? <LightModeIcon fontSize="small" /> : <DarkModeIcon fontSize="small" />}
          </IconButton>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
