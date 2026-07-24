import { useEffect, useState } from 'react';
import { Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { getTrends } from '../api/analytics';

export default function AnalyticsPage() {
  const [trends, setTrends] = useState<Record<string, unknown>[]>([]);

  useEffect(() => {
    getTrends(14).then(setTrends);
  }, []);

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>Analytics</Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Daily Trends (Last 14 Days)</Typography>
              {trends.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No data yet</Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Date</TableCell>
                        <TableCell>Total</TableCell>
                        <TableCell>Passed</TableCell>
                        <TableCell>Failed</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {trends.slice(-14).reverse().map((t, i) => (
                        <TableRow key={i}>
                          <TableCell>{t.date as string}</TableCell>
                          <TableCell>{(t.total as number) || 0}</TableCell>
                          <TableCell sx={{ color: 'success.main' }}>{(t.passed as number) || 0}</TableCell>
                          <TableCell sx={{ color: 'error.main' }}>{(t.failed as number) || 0}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
