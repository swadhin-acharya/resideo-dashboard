import { useLocation, useNavigate } from 'react-router-dom';
import {
  Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Box, Typography, Divider,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TimelineIcon from '@mui/icons-material/Timeline';
import HistoryIcon from '@mui/icons-material/History';
import AssessmentIcon from '@mui/icons-material/Assessment';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import BugReportIcon from '@mui/icons-material/BugReport';
import ExploreIcon from '@mui/icons-material/Explore';
import DevicesIcon from '@mui/icons-material/Devices';
import DeviceThermostatIcon from '@mui/icons-material/DeviceThermostat';
import HubIcon from '@mui/icons-material/Hub';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import PublicIcon from '@mui/icons-material/Public';
import SettingsIcon from '@mui/icons-material/Settings';
import ScheduleIcon from '@mui/icons-material/Schedule';
import PeopleIcon from '@mui/icons-material/People';
import ExtensionIcon from '@mui/icons-material/Extension';
import BuildIcon from '@mui/icons-material/Build';

const DRAWER_WIDTH = 220;

interface NavItem {
  label: string;
  icon: JSX.Element;
  path: string;
  divider?: boolean;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { label: 'Execution Center', icon: <PlayArrowIcon />, path: '/executions/new' },
  { label: 'Live Executions', icon: <TimelineIcon />, path: '/live' },
  { label: 'Execution History', icon: <HistoryIcon />, path: '/executions' },
  { label: 'Reports', icon: <AssessmentIcon />, path: '/reports' },
  { label: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
  { label: 'Flaky Tests', icon: <BugReportIcon />, path: '/flaky-tests' },
  { label: 'Test Explorer', icon: <ExploreIcon />, path: '/test-explorer' },
  { label: 'Devices', icon: <DevicesIcon />, path: '/devices' },
  { label: 'Thermostats', icon: <DeviceThermostatIcon />, path: '/thermostats' },
  { label: 'MQTT Monitor', icon: <HubIcon />, path: '/mqtt' },
  { label: 'Serial Monitor', icon: <MonitorHeartIcon />, path: '/serial' },
  { label: 'Environments', icon: <PublicIcon />, path: '/environments' },
  { label: 'Configurations', icon: <SettingsIcon />, path: '/configurations' },
  { label: 'Schedules', icon: <ScheduleIcon />, path: '/schedules' },
  { label: 'Users', icon: <PeopleIcon />, path: '/users' },
  { label: 'Integrations', icon: <ExtensionIcon />, path: '/integrations' },
  { label: 'System Settings', icon: <BuildIcon />, path: '/settings' },
];

interface SidebarProps {
  open: boolean;
}

export default function Sidebar({ open }: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();

  const selected = (path: string) => location.pathname === path || location.pathname.startsWith(path + '/');

  return (
    <Box
      sx={{
        width: open ? DRAWER_WIDTH : 0,
        flexShrink: 0,
        overflow: 'hidden',
        transition: 'width 0.2s ease',
        whiteSpace: 'nowrap',
      }}
    >
      <Drawer
        variant="permanent"
        anchor="left"
        sx={{
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            position: 'static',
            borderRight: '1px solid',
            borderColor: 'divider',
            bgcolor: 'background.paper',
            height: '100%',
          },
        }}
      >
        <Toolbar sx={{ minHeight: 56, height: 56 }} />
        <Box sx={{ overflow: 'auto', flex: 1, py: 1 }}>
          <Typography variant="caption" color="text.secondary" sx={{ px: 2, mb: 0.5, display: 'block', fontWeight: 600, letterSpacing: 0.5 }}>
            MAIN MENU
          </Typography>
          <List dense disablePadding>
            {NAV_ITEMS.slice(0, 8).map((item) => (
              <ListItemButton
                key={item.path}
                selected={selected(item.path)}
                onClick={() => navigate(item.path)}
                sx={{
                  mx: 1,
                  borderRadius: 1.5,
                  mb: 0.25,
                  py: 0.7,
                  '&.Mui-selected': {
                    bgcolor: 'primary.main',
                    color: '#fff',
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

          <Divider sx={{ mx: 2, my: 1.5 }} />
          <Typography variant="caption" color="text.secondary" sx={{ px: 2, mb: 0.5, display: 'block', fontWeight: 600, letterSpacing: 0.5 }}>
            INFRASTRUCTURE
          </Typography>
          <List dense disablePadding>
            {NAV_ITEMS.slice(8, 12).map((item) => (
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

          <Divider sx={{ mx: 2, my: 1.5 }} />
          <Typography variant="caption" color="text.secondary" sx={{ px: 2, mb: 0.5, display: 'block', fontWeight: 600, letterSpacing: 0.5 }}>
            SETTINGS
          </Typography>
          <List dense disablePadding>
            {NAV_ITEMS.slice(12).map((item) => (
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
