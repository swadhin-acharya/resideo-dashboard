import { Typography, Box, Card, CardContent } from '@mui/material';
import { useLocation } from 'react-router-dom';

const PAGE_NAMES: Record<string, string> = {
  '/live': 'Live Executions',
  '/reports': 'Reports',
  '/analytics': 'Analytics',
  '/flaky-tests': 'Flaky Tests',
  '/test-explorer': 'Test Explorer',
  '/environments': 'Environments',
  '/configurations': 'Configurations',
  '/users': 'Users',
  '/integrations': 'Integrations',
  '/settings': 'System Settings',
  '/mqtt': 'MQTT Monitor',
  '/serial': 'Serial Monitor',
};

export default function PlaceholderPage() {
  const location = useLocation();
  const name = PAGE_NAMES[location.pathname] || location.pathname;
  return (
    <Box>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 3 }}>{name}</Typography>
      <Card>
        <CardContent sx={{ py: 8, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" sx={{ mb: 1 }}>
            {name}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            This page is under development. Check back soon.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
