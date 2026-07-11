import { Typography, Box, Card, CardContent } from '@mui/material';

export default function SchedulesPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 3 }}>Schedules</Typography>
      <Card>
        <CardContent sx={{ py: 8, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" sx={{ mb: 1 }}>Scheduled Executions</Typography>
          <Typography variant="body2" color="text.secondary">This page is under development. Check back soon.</Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
