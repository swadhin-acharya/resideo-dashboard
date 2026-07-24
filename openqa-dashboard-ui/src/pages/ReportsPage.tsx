import { useEffect, useState, useCallback } from 'react';
import {
  Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, IconButton,
  Dialog, DialogTitle, DialogContent, CircularProgress, Tooltip,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DescriptionIcon from '@mui/icons-material/Description';
import CloseIcon from '@mui/icons-material/Close';
import { getReports, getReportByExecution } from '../api/reports';
import api from '../api/client';
import { ReportItem } from '../types/execution';

function formatDate(dateStr: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString() + ' ' + new Date(dateStr).toLocaleTimeString();
}

export default function ReportsPage() {
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewer, setViewer] = useState<{ open: boolean; executionId: string; name: string; html: string; loading: boolean }>({ open: false, executionId: '', name: '', html: '', loading: false });

  const openViewer = async (executionId: string, name: string) => {
    setViewer({ open: true, executionId, name, html: '', loading: true });
    try {
      const reports = await getReportByExecution(executionId);
      if (reports.length === 0) {
        setViewer(prev => ({ ...prev, html: '<p>No report file found for this execution.</p>', loading: false }));
        return;
      }
      const reportId = reports[0].id;
      const resp = await api.get(`/reports/${reportId}/content`, { responseType: 'text' });
      setViewer(prev => ({ ...prev, html: resp.data, loading: false }));
    } catch {
      setViewer(prev => ({ ...prev, html: '<p>Failed to load report</p>', loading: false }));
    }
  };

  const load = useCallback(() => {
    setLoading(true);
    getReports().then(setReports).catch(() => setReports([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Reports</Typography>
        <Button variant="outlined" size="small" onClick={load}>Refresh</Button>
      </Box>

      <Card>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ px: 1, py: 1 }}>Name</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>File</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Date</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }} align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ py: 4 }}>
                      <Typography variant="body2" color="text.secondary">Loading reports...</Typography>
                    </TableCell>
                  </TableRow>
                ) : reports.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ py: 4 }}>
                      <Typography variant="body2" color="text.secondary">No reports available.</Typography>
                    </TableCell>
                  </TableRow>
                ) : reports.map((r) => (
                  <TableRow key={r.id} hover sx={{ '& td': { px: 1, py: 0.75 } }}>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500} sx={{ fontSize: '0.85rem' }}>{r.name}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontSize="0.8rem" color="text.secondary">{r.filePath || '-'}</Typography>
                    </TableCell>
                    <TableCell><Typography variant="body2" fontSize="0.75rem" color="text.secondary">{formatDate(r.createdAt)}</Typography></TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        <Tooltip title="View Report">
                          <IconButton size="small" onClick={() => openViewer(r.executionId, r.name)} sx={{ color: 'primary.main' }}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Download Logs">
                          <IconButton size="small" onClick={() => {
                            const a = document.createElement('a');
                            a.href = `/api/executions/${r.executionId}/logs/download`;
                            a.download = `execution_${r.executionId}.log`;
                            a.click();
                          }} sx={{ color: 'text.secondary' }}>
                            <DescriptionIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      <Dialog open={viewer.open} onClose={() => setViewer(v => ({ ...v, open: false }))} maxWidth="xl" fullWidth>
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" fontWeight={700}>Report: {viewer.name}</Typography>
          <IconButton onClick={() => setViewer(v => ({ ...v, open: false }))} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ height: '80vh', p: 0, '&:first-of-type': { pt: 0 } }}>
          {viewer.loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
              <CircularProgress />
            </Box>
          ) : (
            <iframe srcDoc={viewer.html} title="Report" style={{ width: '100%', height: '100%', border: 'none' }} />
          )}
        </DialogContent>
      </Dialog>
    </Box>
  );
}
