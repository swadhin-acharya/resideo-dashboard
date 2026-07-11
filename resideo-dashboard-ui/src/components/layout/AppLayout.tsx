import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import { useState } from 'react';

const DRAWER_WIDTH = 220;

export default function AppLayout() {
  const [open, setOpen] = useState(true);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Topbar open={open} onToggle={() => setOpen(!open)} />
      <Sidebar open={open} />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          pt: { xs: '56px', sm: '56px' },
          p: 2,
          minHeight: '100vh',
          overflow: 'auto',
          transition: 'margin 0.2s ease',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}
