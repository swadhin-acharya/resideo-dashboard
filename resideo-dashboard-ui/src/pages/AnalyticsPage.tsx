import { useEffect, useState } from 'react';
import { Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { getTrends, getFlakyTests, getMostFailedFeatures, getMostFailedScenarios } from '../api/analytics';

export default function AnalyticsPage() {
  const [trends, setTrends] = useState<Record<string, unknown>[]>([]);
  const [flaky, setFlaky] = useState<Record<string, unknown>[]>([]);
  const [failedFeatures, setFailedFeatures] = useState<Record<string, unknown>[]>([]);
  const [failedScenarios, setFailedScenarios] = useState<Record<string, unknown>[]>([]);

  useEffect(() => {
    getTrends(14).then(setTrends);
    getFlakyTests().then(setFlaky);
    getMostFailedFeatures().then(setFailedFeatures);
    getMostFailedScenarios().then(setFailedScenarios);
  }, []);

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>Analytics</Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
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

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Flaky Tests</Typography>
              {flaky.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  No flaky tests detected. Flaky detection requires multiple execution runs.
                </Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Scenario</TableCell>
                        <TableCell>Runs</TableCell>
                        <TableCell>Passed</TableCell>
                        <TableCell>Failed</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {flaky.map((f, i) => (
                        <TableRow key={i}>
                          <TableCell>{f.scenarioName as string}</TableCell>
                          <TableCell>{(f.totalRuns as number) || 0}</TableCell>
                          <TableCell>{(f.passed as number) || 0}</TableCell>
                          <TableCell>{(f.failed as number) || 0}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Most Failed Features</Typography>
              {failedFeatures.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No data yet</Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Feature</TableCell>
                        <TableCell>Failures</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {failedFeatures.map((f, i) => (
                        <TableRow key={i}>
                          <TableCell>{f.featureName as string}</TableCell>
                          <TableCell>{(f.failures as number) || 0}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Most Failed Scenarios</Typography>
              {failedScenarios.length === 0 ? (
                <Typography variant="body2" color="text.secondary">No data yet</Typography>
              ) : (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Scenario</TableCell>
                        <TableCell>Failures</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {failedScenarios.map((s, i) => (
                        <TableRow key={i}>
                          <TableCell>{s.scenarioName as string}</TableCell>
                          <TableCell>{(s.failures as number) || 0}</TableCell>
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
