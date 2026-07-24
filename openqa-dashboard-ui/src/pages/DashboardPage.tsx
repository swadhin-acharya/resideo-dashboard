import { useEffect, useMemo, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, LinearProgress, Button, IconButton,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useNavigate, useSearchParams } from 'react-router-dom';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import { getExecutions } from '../api/executions';
import { getTrends, getDeviceStats } from '../api/analytics';
import { ExecutionSummary } from '../types/execution';
import StatusBadge from '../components/common/StatusBadge';

echarts.use([LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

export default function DashboardPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [recent, setRecent] = useState<ExecutionSummary[]>([]);
  const [trendData, setTrendData] = useState<Record<string, unknown>[]>([]);
  const [deviceStats, setDeviceStats] = useState<Record<string, unknown>>({});

  const searchQ = searchParams.get('q')?.toLowerCase() || '';

  useEffect(() => {
    getExecutions().then(setRecent).catch(() => {});
    getTrends(7).then(setTrendData).catch(() => {});
    getDeviceStats().then(setDeviceStats).catch(() => {});
  }, []);

  const filtered = useMemo(() => {
    if (!searchQ) return recent;
    return recent.filter(e => e.name?.toLowerCase().includes(searchQ));
  }, [recent, searchQ]);

  const total = filtered.length;
  const passed = filtered.filter(e => e.status === 'PASSED').length;
  const failed = filtered.filter(e => e.status === 'FAILED').length;
  const running = filtered.filter(e => e.status === 'RUNNING').length;
  const passRate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0';

  const cards = [
    { title: 'Total Executions', value: total, color: '#58a6ff' },
    { title: 'Passed', value: passed, color: '#2da44e' },
    { title: 'Failed', value: failed, color: '#da3633' },
    { title: 'Running', value: running, color: '#d29922' },
    { title: 'Pass Rate', value: `${passRate}%`, color: '#8b949e' },
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
    itemStyle: { color: k === 'ANDROID' ? '#2da44e' : '#d29922' },
  }));

  const platformOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', backgroundColor: '#1a1d27', borderColor: '#2d303a', textStyle: { color: '#e1e4e8', fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['55%', '78%'], avoidLabelOverlap: false,
      label: { show: true, color: '#8b949e', fontSize: 11, formatter: '{b}\n{d}%' },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' }, itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.4)' } },
      data: platformData.length > 0 ? platformData : [{ value: 1, name: 'No data', itemStyle: { color: '#2d303a' } }],
    }],
  };

  const passFailData = [
    { name: 'Passed', value: passed, itemStyle: { color: '#2da44e' } },
    { name: 'Failed', value: failed, itemStyle: { color: '#da3633' } },
    { name: 'Running', value: running, itemStyle: { color: '#d29922' } },
  ].filter(d => d.value > 0);

  const passFailOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)', backgroundColor: '#1a1d27', borderColor: '#2d303a', textStyle: { color: '#e1e4e8', fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['55%', '78%'], avoidLabelOverlap: false,
      label: { show: true, color: '#8b949e', fontSize: 11, formatter: '{b}\n{d}%' },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' }, itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.4)' } },
      data: passFailData.length > 0 ? passFailData : [{ value: 1, name: 'No data', itemStyle: { color: '#2d303a' } }],
    }],
  };

  return (
    <Box sx={{ mt: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Dashboard</Typography>
      </Box>

      <Grid container spacing={1.5} sx={{ mb: 2.5 }}>
        {cards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4, lg: 2 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 }, display: 'flex', flexDirection: 'column', height: '100%' }}>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.6, fontSize: '0.68rem' }}>
                  {c.title}
                </Typography>
                <Typography variant="h5" fontWeight={700} sx={{ mt: 0.5, mb: 0.75 }}>{c.value}</Typography>
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
                <Typography variant="h6" sx={{ fontSize: '0.95rem' }}>Recent Executions</Typography>
                <Button size="small" variant="outlined" startIcon={<PlayArrowIcon />} onClick={() => navigate('/executions/new')} sx={{ height: 30, fontSize: '0.75rem' }}>
                  New Execution
                </Button>
              </Box>
              <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
                <Table size="small" sx={{ minWidth: 700 }}>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ px: 1, py: 1 }}>Name</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Type</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Platform</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Status</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Progress</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }}>Duration</TableCell>
                      <TableCell sx={{ px: 1, py: 1 }} align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filtered.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                          <Typography variant="body2" color="text.secondary">{searchQ ? 'No executions match your search.' : 'No executions yet.'}</Typography>
                        </TableCell>
                      </TableRow>
                    ) : filtered.slice(0, 10).map((ex) => (
                      <TableRow key={ex.id} hover sx={{ cursor: 'pointer', '& td': { px: 1, py: 0.75 } }} onClick={() => navigate(`/executions/${ex.id}`)}>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500} sx={{ fontSize: '0.8rem' }}>{ex.name}</Typography>
                        </TableCell>
                        <TableCell><Typography variant="body2" fontSize="0.8rem">{ex.executionType}</Typography></TableCell>
                        <TableCell>
                          <Chip label={ex.platform || 'N/A'} size="small" color={ex.platform === 'ANDROID' ? 'success' : 'default'} variant="outlined" sx={{ height: 20, fontSize: '0.65rem' }} />
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
                          <IconButton size="small" onClick={(e) => { e.stopPropagation(); navigate(`/executions/${ex.id}`); }} sx={{ color: 'text.secondary' }}><OpenInNewIcon sx={{ fontSize: 16 }} /></IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, lg: 4 }}>
          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>Pass / Fail</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <ReactEChartsCore echarts={echarts} option={passFailOption} style={{ height: 180, width: 200 }} />
              </Box>
            </CardContent>
          </Card>
          <Card sx={{ mb: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Typography variant="h6" sx={{ mb: 1.5, fontSize: '0.95rem' }}>By Platform</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <ReactEChartsCore echarts={echarts} option={platformOption} style={{ height: 180, width: 200 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
