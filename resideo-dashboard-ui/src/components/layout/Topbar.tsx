import {
  AppBar, Toolbar, IconButton, Typography, Box, InputBase, Avatar, Badge, alpha, Button,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import LogoutIcon from '@mui/icons-material/Logout';
import { useThemeMode } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';
import ProjectSwitcher from '../auth/ProjectSwitcher';
import { useNavigate } from 'react-router-dom';

interface TopbarProps {
  open: boolean;
  onToggle: () => void;
}

export default function Topbar({ open, onToggle }: TopbarProps) {
  const { mode, toggle } = useThemeMode();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        bgcolor: 'background.paper',
        borderBottom: '1px solid',
        borderColor: 'divider',
        zIndex: (t) => t.zIndex.drawer + 1,
      }}
    >
      <Toolbar sx={{ gap: 2, minHeight: { xs: 56, sm: 56, md: 56 }, height: { xs: 56, sm: 56, md: 56 } }}>
        <IconButton edge="start" onClick={onToggle} sx={{ color: 'text.secondary' }}>
          <MenuIcon />
        </IconButton>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mr: 1 }}>
          <img src="/assets/resideo-logo.png" alt="Resideo" height={36} />
          <Typography variant="h6" fontWeight={700} color="text.primary" sx={{ fontSize: '1.05rem', letterSpacing: '-0.3px' }}>
            Test Dashboard
          </Typography>
        </Box>

        {user && <ProjectSwitcher />}

        <Box
          sx={{
            flex: 1,
            maxWidth: 420,
            display: { xs: 'none', md: 'flex' },
            alignItems: 'center',
            bgcolor: (t) => alpha(t.palette.text.secondary, 0.08),
            borderRadius: 1.5,
            px: 1.5,
          }}
        >
          <SearchIcon sx={{ color: 'text.secondary', fontSize: 18, mr: 1 }} />
          <InputBase
            placeholder="Search executions, features..."
            sx={{ flex: 1, fontSize: '0.8125rem', py: 0.625, color: 'text.primary' }}
          />
        </Box>

        <Box sx={{ flex: 1 }} />

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          <IconButton onClick={toggle} sx={{ color: 'text.secondary' }} size="small">
            {mode === 'dark' ? <LightModeIcon fontSize="small" /> : <DarkModeIcon fontSize="small" />}
          </IconButton>

          <IconButton sx={{ color: 'text.secondary' }} size="small">
            <Badge badgeContent={3} color="error" variant="dot" slotProps={{ badge: { sx: { width: 8, height: 8, minWidth: 8 } } }}>
              <NotificationsNoneIcon fontSize="small" />
            </Badge>
          </IconButton>

          {user ? (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, ml: 0.5 }}>
              <Avatar sx={{ width: 28, height: 28, bgcolor: 'primary.main', fontSize: '0.7rem', fontWeight: 700 }}>
                {user.displayName?.charAt(0).toUpperCase() || 'U'}
              </Avatar>
              <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
                <Typography variant="body2" fontWeight={600} color="text.primary" lineHeight={1.15} sx={{ fontSize: '0.8rem' }}>
                  {user.displayName || user.username}
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
                  {user.globalRole}
                </Typography>
              </Box>
              <IconButton size="small" onClick={() => { logout(); navigate('/login'); }} sx={{ color: 'text.secondary' }}>
                <LogoutIcon sx={{ fontSize: 16 }} />
              </IconButton>
            </Box>
          ) : (
            <Button size="small" variant="outlined" onClick={() => navigate('/login')} sx={{ height: 30, fontSize: '0.75rem' }}>
              Sign In
            </Button>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}
