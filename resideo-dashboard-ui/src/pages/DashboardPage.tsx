import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, LinearProgress, Button, IconButton,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useNavigate } from 'react-router-dom';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import { getSummary, getRunning, getExecutions } from '../api/executions';
import {
  getTrends, getMostFailedFeatures, getMostFailedScenarios, getDeviceStats,
} from '../api/analytics';
import { ExecutionSummary, Execution } from '../types/execution';
import StatusBadge from '../components/common/StatusBadge';

echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer]);

const MOCK_DEVICES = [
  { name: 'Pixel 7', platform: 'Android', os: '14.0', status: 'Available' },
  { name: 'iPhone 15', platform: 'iOS', os: '17.0', status: 'Running' },
  { name: 'Galaxy S24', platform: 'Android', os: '14.0', status: 'Offline' },
  { name: 'iPad Pro', platform: 'iOS', os: '17.2', status: 'Available' },
  { name: 'Pixel 8', platform: 'Android', os: '15.0', status: 'Running' },
];

const MOCK_THERMOSTATS = [
  { id: 'TH-001', fw: '2.4.1', status: 'Online', port: 'COM3' },
  { id: 'TH-002', fw: '2.4.0', status: 'Busy', port: 'COM4' },
  { id: 'TH-003', fw: '2.3.8', status: 'Offline', port: 'COM5' },
  { id: 'TH-004', fw: '2.4.1', status: 'Online', port: 'COM6' },
];

const MQTT_ITEMS = [
  { label: 'Broker Status', value: 'Connected', color: 'success.main' },
  { label: 'Connected Clients', value: '4', color: '' },
  { label: 'Active Topics', value: '23', color: '' },
  { label: 'Messages / min', value: '142', color: '' },
  { label: 'Subscription Health', value: '98%', color: 'success.main' },
  { label: 'Last Message', value: '2s ago', color: '' },
];

export default function DashboardPage() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<ExecutionSummary | null>(null);
  const [recent, setRecent] = useState<Execution[]>([]);
  const [running, setRunning] = useState<Execution[]>([]);
  const [trendData, setTrendData] = useState<Record<string, unknown>[]>([]);
  const [failedFeatures, setFailedFeatures] = useState<Record<string, unknown>[]>([]);
  const [failedScenarios, setFailedScenarios] = useState<Record<string, unknown>[]>([]);
  const [deviceStats, setDeviceStats] = useState<Record<string, unknown>>({});

  useEffect(() => {
    getSummary().then(setSummary).catch(() => {});
    getRunning().then(setRunning).catch(() => {});
    getExecutions({ page: 0, size: 10 }).then(r => setRecent(r.content)).catch(() => {});
    getTrends(7).then(setTrendData).catch(() => {});
    getMostFailedFeatures().then(setFailedFeatures).catch(() => {});
    getMostFailedScenarios().then(setFailedScenarios).catch(() => {});
    getDeviceStats().then(setDeviceStats).catch(() => {});
  }, []);

  const s = summary;
  const cards = [
    { title: 'Total Executions', value: s?.totalExecutions ?? 0, change: '', up: true, color: '#58a6ff' },
    { title: 'Passed', value: s?.passed ?? 0, change: '', up: true, color: '#2da44e' },
    { title: 'Failed', value: s?.failed ?? 0, change: '', up: false, color: '#da3633' },
    { title: 'Running', value: s?.running ?? 0, change: '', up: true, color: '#d29922' },
    { title: 'Pass Rate', value: s?.passRate != null ? `${s.passRate.toFixed(1)}%` : '0%', change: '', up: true, color: '#8b949e' },
  ];

  const trendDays = trendData.map((d: Record<string, unknown>) => {
    const date = (d.date as string) || '';
    return date.length > 10 ? date.substring(5, 10) : date;
  });
  const trendTotals = trendData.map((d: Record<string, unknown>) => (d.total as number) || 0);
  const trendPassed = trendData.map((d: Record<string, unknown>) => (d.passed as number) || 0);
  const trendFailed = trendData.map((d: Record<string, unknown>) => (d.failed as number) || 0);

  const trendOption = {
    tooltip: { trigger: 'axis', backgroundColor: '#1a1d27', borderColor: '#2d303a', textStyle: { color: '#e1e4e8', fontSize: 12 } },
    legend: { data: ['Total', 'Passed', 'Failed'], textStyle: { color: '#8b949e', fontSize: 11 }, bottom: 0, itemWidth: 10, itemHeight: 10 },
    grid: { left: 44, right: 16, top: 12, bottom: 40 },
    xAxis: { type: 'category' as const, data: trendDays, axisLabel: { color: '#8b949e', fontSize: 11 }, axisLine: { lineStyle: { color: '#2d303a' } }, axisTick: { show: false } },
    yAxis: { type: 'value' as const, axisLabel: { color: '#8b949e', fontSize: 11 }, splitLine: { lineStyle: { color: '#2d303a', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false } },
    series: [
      { name: 'Total', type: 'line', data: trendTotals, smooth: true, lineStyle: { width: 2, color: '#58a6ff' }, symbol: 'circle', symbolSize: 4, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#58a6ff20' }, { offset: 1, color: '#58a6ff02' }]) } },
      { name: 'Passed', type: 'line', data: trendPassed, smooth: true, lineStyle: { width: 2, color: '#2da44e' }, symbol: 'circle', symbolSize: 4, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#2da44e20' }, { offset: 1, color: '#2da44e02' }]) } },
      { name: 'Failed', type: 'line', data: trendFailed, smooth: true, lineStyle: { width: 2, color: '#da3633' }, symbol: 'circle', symbolSize: 4 },
    ],
  };

  const byPlatform = (deviceStats as any).byPlatform || {};
  const platformData = Object.entries(byPlatform).map(([k, v]) => ({
    name: k, value: v as number,
    itemStyle: { color: k === 'ANDROID' ? '#2da44e' : k === 'IOS' ? '#58a6ff' : '#d29922' },
  }));
  const platformTotal = platformData.reduce((a, b) => a + b.value, 0);

  const platformOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', backgroundColor: '#1a1d27', borderColor: '#2d303a', textStyle: { color: '#e1e4e8', fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['55%', '78%'], avoidLabelOverlap: false,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' }, itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.4)' } },
      data: platformData.length > 0 ? platformData : [{ value: 1, name: 'No data', itemStyle: { color: '#2d303a' } }],
    }],
  };

  const featuresOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#1a1d27', borderColor: '#2d303a', textStyle: { color: '#e1e4e8', fontSize: 12 } },
    grid: { left: 110, right: 16, top: 4, bottom: 4 },
    xAxis: { type: 'value' as const, axisLabel: { color: '#8b949e', fontSize: 10 }, splitLine: { lineStyle: { color: '#2d303a', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: 'category' as const, data: failedFeatures.map((f: Record<string, unknown>) => f.featureName as string).reverse(), axisLabel: { color: '#e1e4e8', fontSize: 11 }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{ type: 'bar', data: failedFeatures.map((f: Record<string, unknown>) => (f.failures as number) || 0).reverse(), itemStyle: { color: '#da3633', borderRadius: [0, 3, 3, 0] }, barWidth: 14 }],
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Dashboard</Typography>
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          {['Today', 'Last 7 Days', 'Last 30 Days'].map(t => (
            <Chip key={t} label={t} size="small" variant={t === 'Last 7 Days' ? 'filled' : 'outlined'} color={t === 'Last 7 Days' ? 'primary' : 'default'} sx={{ height: 26, fontSize: '0.75rem' }} />
          ))}
        </Box>
      </Box>

      <Grid container spacing={1.5} sx={{ mb: 2.5 }}>
        {cards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 }, display: 'flex', flexDirection: 'column', height: '100%' }}>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.6, fontSize: '0.68rem' }}>
                  {c.title}
                </Typography>
                <Typography variant="h5" fontWeight={700} sx={{ mt: 0.5, mb: 0.75 }}>
                  {c.value}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 'auto' }}>
                  <Typography variant="caption" sx={{ color: c.change === '' ? 'text.secondary' : c.up ? 'success.main' : 'error.main', fontWeight: 600, fontSize: '0.7rem' }}>
                    {c.change || '—'}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>Execution Trend</Typography>
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  {['Today', '7 Days', '30 Days'].map(t => (
                    <Chip key={t} label={t} size="small" variant={t === '7 Days' ? 'filled' : 'outlined'} color={t === '7 Days' ? 'primary' : 'default'} sx={{ height: 24, fontSize: '0.7rem' }} />
                  ))}
                </Box>
              </Box>
              {trendData.length === 0 ? (
                <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>No trend data yet</Typography>
              ) : (
                <ReactEChartsCore echarts={echarts} option={trendOption} style={{ height: 260 }} />
              )}
            </CardContent>
          </Card>

          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>Live Executions</Typography>
                <Button size="small" variant="outlined" startIcon={<PlayArrowIcon />} onClick={() => navigate('/executions/new')} sx={{ height: 30, fontSize: '0.75rem' }}>
                  New Execution
                </Button>
              </Box>
              <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
                <Table size="small" sx={{ minWidth: 700 }}>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ px: 1, py: 1 }}>Execution ID</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Suite Type</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Triggered By</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Environment</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Platform</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Status</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Progress</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Duration</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }} align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {recent.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={9} align="center" sx={{ py: 4 }}>
                          <Typography variant="body2" color="text.secondary">No executions yet. Start your first execution.</Typography>
                        </TableCell>
                      </TableRow>
                    ) : recent.map((ex) => (
                      <TableRow key={ex.id} hover sx={{ cursor: 'pointer', '& td': { px: 1, py: 0.75 } }} onClick={() => navigate(`/executions/${ex.id}`)}>
                        <TableCell>
                          <Typography variant="body2" fontFamily="monospace" fontSize="0.7rem" color="text.secondary">{ex.id.slice(0, 8)}</Typography>
                        </TableCell>
                        <TableCell><Typography variant="body2" fontSize="0.8rem">{ex.executionType || 'REGRESSION'}</Typography></TableCell>
                        <TableCell><Typography variant="body2" fontSize="0.8rem">{ex.triggeredBy || 'Auto'}</Typography></TableCell>
                        <TableCell>
                          <Chip label={ex.environment || 'QA'} size="small" variant="outlined" sx={{ height: 20, fontSize: '0.65rem' }} />
                        </TableCell>
                        <TableCell>
                          <Chip label={ex.platform || 'N/A'} size="small" color={ex.platform === 'ANDROID' ? 'success' : ex.platform === 'IOS' ? 'info' : 'default'} variant="outlined" sx={{ height: 20, fontSize: '0.65rem' }} />
                        </TableCell>
                        <TableCell><StatusBadge status={ex.status} /></TableCell>
                        <TableCell sx={{ minWidth: 120 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <LinearProgress variant="determinate" value={ex.totalCount > 0 ? ((ex.passCount + ex.failCount) / ex.totalCount) * 100 : 0} sx={{ flex: 1, height: 4, borderRadius: 2 }} />
                            <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.65rem', whiteSpace: 'nowrap' }}>
                              {ex.passCount + ex.failCount}/{ex.totalCount}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell><Typography variant="body2" fontSize="0.8rem" color="text.secondary">{ex.durationMs ? `${(ex.durationMs / 1000).toFixed(1)}s` : '—'}</Typography></TableCell>
                        <TableCell align="right">
                          <IconButton size="small" onClick={(e) => { e.stopPropagation(); }} sx={{ color: 'text.secondary' }}><OpenInNewIcon sx={{ fontSize: 16 }} /></IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          <Grid container spacing={2.5}>
            <Grid size={{ xs: 12, md: 6 }}>
              <Card sx={{ height: '100%' }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 }, height: '100%', display: 'flex', flexDirection: 'column' }}>
                  <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Top Failed Features</Typography>
                  <Box sx={{ flex: 1 }}>
                    {failedFeatures.length === 0 ? (
                      <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>No failures yet</Typography>
                    ) : (
                      <ReactEChartsCore echarts={echarts} option={featuresOption} style={{ height: '100%', minHeight: 200 }} />
                    )}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <Card sx={{ height: '100%' }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 }, height: '100%', display: 'flex', flexDirection: 'column' }}>
                  <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Recent Failures</Typography>
                  <Box sx={{ flex: 1 }}>
                    {failedScenarios.length === 0 ? (
                      <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>No failures yet</Typography>
                    ) : failedScenarios.slice(0, 5).map((f: Record<string, unknown>, i: number) => (
                      <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 0.625, borderBottom: i < Math.min(failedScenarios.length, 5) - 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
                        <Box sx={{ flex: 1, minWidth: 0, mr: 1 }}>
                          <Typography variant="body2" fontWeight={500} noWrap sx={{ fontSize: '0.8rem' }}>{f.scenarioName as string}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
                          <Chip label={String(f.failures) + ' failures'} size="small" color="error" sx={{ height: 18, fontSize: '0.6rem', fontWeight: 600 }} />
                        </Box>
                      </Box>
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Grid>

        <Grid size={{ xs: 12, lg: 4 }}>
          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Execution by Platform</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                  <ReactEChartsCore echarts={echarts} option={platformOption} style={{ height: 180, width: 180 }} />
                  <Box sx={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
                    <Typography variant="h6" fontWeight={700} sx={{ fontSize: '1.3rem', lineHeight: 1.1 }}>{platformTotal}</Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>Total</Typography>
                  </Box>
                </Box>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 1.5 }}>
                {platformData.length > 0 ? platformData.map((i: any) => (
                  <Box key={i.name} sx={{ textAlign: 'center' }}>
                    <Typography variant="body2" fontWeight={600} sx={{ fontSize: '0.9rem' }}>{i.value}</Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.68rem' }}>{i.name}</Typography>
                  </Box>
                )) : (
                  <Typography variant="caption" color="text.secondary">No execution data</Typography>
                )}
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Device Lab</Typography>
              <TableContainer sx={{ bgcolor: 'transparent' }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Device</TableCell>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Platform</TableCell>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {MOCK_DEVICES.map(d => (
                      <TableRow key={d.name} sx={{ '& td': { px: 0.5, py: 0.625 } }}>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500} sx={{ fontSize: '0.8rem' }}>{d.name}</Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.65rem' }}>OS {d.os}</Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={d.platform} size="small" variant="outlined" color={d.platform === 'Android' ? 'success' : 'info'} sx={{ height: 18, fontSize: '0.6rem' }} />
                        </TableCell>
                        <TableCell>
                          <Chip label={d.status} size="small" color={d.status === 'Available' ? 'success' : d.status === 'Running' ? 'info' : 'default'} sx={{ height: 18, fontSize: '0.6rem', fontWeight: 600 }} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Thermostat Lab</Typography>
              <TableContainer sx={{ bgcolor: 'transparent' }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ px: 0.5, py: 1 }}>ID</TableCell>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Firmware</TableCell>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Status</TableCell>
                      <TableCell sx={{ px: 0.5, py: 1 }}>Port</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {MOCK_THERMOSTATS.map(t => (
                      <TableRow key={t.id} sx={{ '& td': { px: 0.5, py: 0.625 } }}>
                        <TableCell><Typography variant="body2" fontFamily="monospace" sx={{ fontSize: '0.75rem' }}>{t.id}</Typography></TableCell>
                        <TableCell><Typography variant="caption" sx={{ fontSize: '0.75rem' }}>v{t.fw}</Typography></TableCell>
                        <TableCell>
                          <Chip label={t.status} size="small" color={t.status === 'Online' ? 'success' : t.status === 'Busy' ? 'warning' : 'default'} sx={{ height: 18, fontSize: '0.6rem', fontWeight: 600 }} />
                        </TableCell>
                        <TableCell><Typography variant="caption" fontFamily="monospace" color="text.secondary" sx={{ fontSize: '0.7rem' }}>{t.port}</Typography></TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>

          <Card>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>MQTT Monitor</Typography>
              <Box>
                {MQTT_ITEMS.map((item, i) => (
                  <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.625, borderBottom: i < MQTT_ITEMS.length - 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
                    <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.8rem' }}>{item.label}</Typography>
                    <Typography variant="body2" fontWeight={600} color={item.color || 'text.primary'} sx={{ fontSize: '0.8rem' }}>{item.value}</Typography>
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}