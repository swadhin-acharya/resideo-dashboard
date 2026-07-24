import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Typography, Box, Card, CardContent, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton, Button,
  Collapse,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip as RechartsTooltip } from 'recharts';
import { getExecution } from '../api/executions';
import api from '../api/client';
import { Execution } from '../types/execution';
import StatusBadge from '../components/common/StatusBadge';

interface ScenarioStep {
  id: number;
  keyword: string;
  stepName: string;
  status: string;
  durationMs: number;
  logText: string;
}

const COLORS = { PASSED: '#2e7d32', FAILED: '#d32f2f', SKIPPED: '#ed6c02' };

function formatDuration(ms: number): string {
  if (!ms) return '-';
  const s = Math.floor(ms / 1000);
  const m = Math.floor(s / 60);
  const h = Math.floor(m / 60);
  if (h > 0) return `${h}h ${m % 60}m ${s % 60}s`;
  if (m > 0) return `${m}m ${s % 60}s`;
  return `${s}s`;
}

export default function ExecutionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [exec, setExec] = useState<Execution | null>(null);
  const [logLines, setLogLines] = useState<string[]>([]);
  const [logsLoaded, setLogsLoaded] = useState(false);
  const [expandedScenario, setExpandedScenario] = useState<string | null>(null);
  const [scenarioSteps, setScenarioSteps] = useState<Record<string, ScenarioStep[]>>({});
  const [loadingSteps, setLoadingSteps] = useState<Record<string, boolean>>({});

  useEffect(() => {
    if (id) getExecution(id).then(setExec);
  }, [id]);

  useEffect(() => {
    if (!id || logsLoaded) return;
    api.get(`/executions/${id}/logs`).then(r => {
      const logs: { level: string; message: string; timestamp: string }[] = r.data;
      if (logs.length > 0) setLogLines(logs.map(l => l.message));
      setLogsLoaded(true);
    }).catch(() => setLogsLoaded(true));
  }, [id, logsLoaded]);

  const handleScenarioClick = async (scenarioId: string) => {
    if (expandedScenario === scenarioId) {
      setExpandedScenario(null);
      return;
    }
    setExpandedScenario(scenarioId);
    if (!scenarioSteps[scenarioId] && !loadingSteps[scenarioId]) {
      setLoadingSteps(prev => ({ ...prev, [scenarioId]: true }));
      try {
        const res = await api.get(`/executions/${id}/scenarios/${scenarioId}/steps`);
        setScenarioSteps(prev => ({ ...prev, [scenarioId]: res.data }));
      } catch {
        setScenarioSteps(prev => ({ ...prev, [scenarioId]: [] }));
      }
      setLoadingSteps(prev => ({ ...prev, [scenarioId]: false }));
    }
  };

  if (!exec) return <Typography>Loading...</Typography>;

  const pieData = [
    { name: 'Passed', value: exec.passCount, color: COLORS.PASSED },
    { name: 'Failed', value: exec.failCount, color: COLORS.FAILED },
    { name: 'Skipped', value: exec.skipCount, color: COLORS.SKIPPED },
  ].filter(d => d.value > 0);

  const isRunning = exec.status === 'RUNNING' || exec.status === 'PENDING';
  const showPie = pieData.length > 0;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h4">{exec.name || 'Execution Detail'}</Typography>
          <StatusBadge status={exec.status} />
        </Box>
        <Button variant="outlined" size="small" startIcon={<PlayArrowIcon />}
          onClick={() => navigate('/executions/new')}>
          New
        </Button>
      </Box>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Grid container spacing={2}>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">Platform</Typography>
                  <Typography fontWeight={600}>{exec.platform || '-'}</Typography>
                </Grid>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">Execution Type</Typography>
                  <Typography fontWeight={600}>{exec.executionType || '-'}</Typography>
                </Grid>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">Branch</Typography>
                  <Typography fontWeight={600}>{exec.branch || '-'}</Typography>
                </Grid>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">Duration</Typography>
                  <Typography fontWeight={600}>{formatDuration(exec.durationMs)}</Typography>
                </Grid>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">Start Time</Typography>
                  <Typography fontWeight={600}>
                    {exec.startTime ? new Date(exec.startTime).toLocaleString() : '-'}
                  </Typography>
                </Grid>
                <Grid size={4}>
                  <Typography variant="body2" color="text.secondary">End Time</Typography>
                  <Typography fontWeight={600}>
                    {exec.endTime ? new Date(exec.endTime).toLocaleString() : isRunning ? 'In progress...' : '-'}
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ mb: 3, height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Results</Typography>
              <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3, mb: 2 }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h3" color="success.main" fontWeight={700}>{exec.passCount}</Typography>
                  <Typography variant="caption" color="text.secondary">Passed</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h3" color="error.main" fontWeight={700}>{exec.failCount}</Typography>
                  <Typography variant="caption" color="text.secondary">Failed</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h3" color="warning.main" fontWeight={700}>{exec.skipCount}</Typography>
                  <Typography variant="caption" color="text.secondary">Skipped</Typography>
                </Box>
              </Box>
              {showPie && (
                <ResponsiveContainer width="100%" height={180}>
                  <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                      {pieData.map((entry, i) => (
                        <Cell key={i} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {exec.features && exec.features.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Features ({exec.features.length})</Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Feature</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Passed</TableCell>
                    <TableCell>Failed</TableCell>
                    <TableCell>Duration</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {exec.features.map((f) => (
                    <TableRow key={f.id}>
                      <TableCell>{f.featureName}</TableCell>
                      <TableCell><StatusBadge status={f.status} /></TableCell>
                      <TableCell>{f.passCount}</TableCell>
                      <TableCell>{f.failCount}</TableCell>
                      <TableCell>{formatDuration(f.durationMs)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {exec.scenarios && exec.scenarios.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Scenarios ({exec.scenarios.length})</Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: 40 }}></TableCell>
                    <TableCell>Scenario</TableCell>
                    <TableCell>Tags</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Duration</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {exec.scenarios.map((s) => (
                    <>
                      <TableRow key={s.id} hover sx={{ cursor: 'pointer', '& td': { px: 1, py: 0.75 } }}
                        onClick={() => handleScenarioClick(s.id)}>
                        <TableCell>
                          <IconButton size="small" sx={{ p: 0 }}>
                            {expandedScenario === s.id ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                          </IconButton>
                        </TableCell>
                        <TableCell>{s.scenarioName}</TableCell>
                        <TableCell>
                          {s.tags ? s.tags.split(',').map((t, i) => (
                            <Chip key={i} label={t.trim()} size="small" variant="outlined" sx={{ mr: 0.3, mb: 0.3 }} />
                          )) : '-'}
                        </TableCell>
                        <TableCell><StatusBadge status={s.status} /></TableCell>
                        <TableCell>{formatDuration(s.durationMs)}</TableCell>
                      </TableRow>
                      <TableRow key={`${s.id}-steps`}>
                        <TableCell colSpan={5} sx={{ p: 0 }}>
                          <Collapse in={expandedScenario === s.id}>
                            <Box sx={{ p: 2, bgcolor: 'action.hover' }}>
                              {loadingSteps[s.id] ? (
                                <Typography variant="body2" color="text.secondary">Loading steps...</Typography>
                              ) : scenarioSteps[s.id] && scenarioSteps[s.id].length > 0 ? (
                                <Table size="small">
                                  <TableHead>
                                    <TableRow>
                                      <TableCell sx={{ fontWeight: 600 }}>Step</TableCell>
                                      <TableCell sx={{ fontWeight: 600, width: 80 }}>Status</TableCell>
                                      <TableCell sx={{ fontWeight: 600, width: 80 }}>Duration</TableCell>
                                    </TableRow>
                                  </TableHead>
                                  <TableBody>
                                    {scenarioSteps[s.id].map((step, i) => (
                                      <TableRow key={step.id || i}>
                                        <TableCell>
                                          <Typography variant="body2" sx={{ fontSize: '0.8rem' }}>
                                            {step.keyword} {step.stepName}
                                          </Typography>
                                          {step.logText && (
                                            <Typography variant="caption" color="text.secondary"
                                              sx={{ fontSize: '0.7rem', display: 'block', mt: 0.5, fontFamily: 'monospace', whiteSpace: 'pre-wrap' }}>
                                              {step.logText}
                                            </Typography>
                                          )}
                                        </TableCell>
                                        <TableCell><StatusBadge status={step.status} /></TableCell>
                                        <TableCell>
                                          <Typography variant="caption" color="text.secondary">{formatDuration(step.durationMs)}</Typography>
                                        </TableCell>
                                      </TableRow>
                                    ))}
                                  </TableBody>
                                </Table>
                              ) : (
                                <Typography variant="body2" color="text.secondary">No steps recorded</Typography>
                              )}
                            </Box>
                          </Collapse>
                        </TableCell>
                      </TableRow>
                    </>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>Execution Log</Typography>
          <Box sx={{ bgcolor: '#1e1e1e', color: '#d4d4d4', p: 2, borderRadius: 1, fontFamily: 'monospace', fontSize: '0.75rem', maxHeight: 400, overflow: 'auto', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
            {logLines.length === 0 ? (
              <Typography sx={{ color: '#888' }}>No logs available</Typography>
            ) : logLines.map((line, i) => (
              <Box key={i} sx={{
                color: line.includes('ERROR') || line.includes('FAILED') ? '#f48771' :
                       line.includes('WARN') ? '#cca700' :
                       line.includes('PASSED') ? '#89d185' : '#d4d4d4'
              }}>
                {line}
              </Box>
            ))}
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
