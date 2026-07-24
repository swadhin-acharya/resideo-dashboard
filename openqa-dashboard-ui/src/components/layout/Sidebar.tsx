import { useLocation, useNavigate } from 'react-router-dom';
import {
  Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Box, Typography,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import HistoryIcon from '@mui/icons-material/History';
import AssessmentIcon from '@mui/icons-material/Assessment';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import DevicesIcon from '@mui/icons-material/Devices';

const DRAWER_WIDTH = 220;

const NAV_ITEMS = [
  { label: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { label: 'New Execution', icon: <PlayArrowIcon />, path: '/executions/new' },
  { label: 'Execution History', icon: <HistoryIcon />, path: '/executions' },
  { label: 'Reports', icon: <AssessmentIcon />, path: '/reports' },
  { label: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
  { label: 'Devices', icon: <DevicesIcon />, path: '/devices' },
];

interface SidebarProps {
  open: boolean;
}

export default function Sidebar({ open }: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const selected = (path: string) => location.pathname === path || location.pathname.startsWith(path + '/');

  return (
    <Box sx={{ width: open ? DRAWER_WIDTH : 0, flexShrink: 0, overflow: 'hidden', transition: 'width 0.2s ease', whiteSpace: 'nowrap' }}>
      <Drawer
        variant="permanent" anchor="left"
        sx={{
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH, boxSizing: 'border-box', position: 'static',
            borderRight: '1px solid', borderColor: 'divider', bgcolor: 'background.paper', height: '100%',
          },
        }}
      >
        <Toolbar sx={{ minHeight: 56, height: 56 }} />
        <Box sx={{ overflow: 'auto', flex: 1, py: 1 }}>
          <Typography variant="caption" color="text.secondary" sx={{ px: 2, mb: 0.5, display: 'block', fontWeight: 600, letterSpacing: 0.5 }}>
            MENU
          </Typography>
          <List dense disablePadding>
            {NAV_ITEMS.map((item) => (
              <ListItemButton
                key={item.path}
                selected={selected(item.path)}
                onClick={() => navigate(item.path)}
                sx={{
                  mx: 1, borderRadius: 1.5, mb: 0.25, py: 0.7,
                  '&.Mui-selected': {
                    bgcolor: 'primary.main', color: '#fff',
                    '& .MuiListItemIcon-root': { color: '#fff' },
                    '&:hover': { bgcolor: 'primary.dark' },
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 34, fontSize: 18 }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: '0.8125rem', fontWeight: selected(item.path) ? 600 : 400 }} />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Drawer>
    </Box>
  );
}
