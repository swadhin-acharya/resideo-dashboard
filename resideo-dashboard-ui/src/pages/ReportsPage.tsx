import { useEffect, useState, useCallback } from 'react';
import {
  Typography, Box, Card, CardContent, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Snackbar, Alert, Tooltip, LinearProgress, CircularProgress,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EmailIcon from '@mui/icons-material/Email';
import DescriptionIcon from '@mui/icons-material/Description';
import CloseIcon from '@mui/icons-material/Close';
import { getReports, emailReport, ReportItem } from '../api/reports';
import api from '../api/client';
import StatusBadge from '../components/common/StatusBadge';

function formatDuration(ms: number): string {
  if (!ms) return '-';
  const s = Math.floor(ms / 1000);
  const m = Math.floor(s / 60);
  const h = Math.floor(m / 60);
  if (h > 0) return `${h}h ${m % 60}m ${s % 60}s`;
  if (m > 0) return `${m}m ${s % 60}s`;
  return `${s}s`;
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString() + ' ' + d.toLocaleTimeString();
}

async function downloadFile(url: string, filename: string) {
  const resp = await api.get(url, { responseType: 'blob' });
  const blob = resp.data;
  const blobUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = blobUrl;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(blobUrl);
}

export default function ReportsPage() {
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [emailDialog, setEmailDialog] = useState<{ open: boolean; id: string; name: string }>({ open: false, id: '', name: '' });
  const [emailTo, setEmailTo] = useState('');
  const [sending, setSending] = useState(false);
  const [snack, setSnack] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });
  const [viewer, setViewer] = useState<{ open: boolean; id: string; name: string; html: string; loading: boolean }>({ open: false, id: '', name: '', html: '', loading: false });

  const openViewer = async (id: string, name: string) => {
    setViewer({ open: true, id, name, html: '', loading: true });
    try {
      const resp = await api.get(`/executions/${id}/report`, { responseType: 'text' });
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

  const handleEmailSend = async () => {
    if (!emailTo.trim()) return;
    setSending(true);
    try {
      await emailReport(emailDialog.id, emailTo.trim());
      setSnack({ open: true, message: 'Report emailed to ' + emailTo, severity: 'success' });
      setEmailDialog({ open: false, id: '', name: '' });
      setEmailTo('');
    } catch {
      setSnack({ open: true, message: 'Failed to send email', severity: 'error' });
    }
    setSending(false);
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Report Center</Typography>
        <Button variant="outlined" size="small" onClick={load}>Refresh</Button>
      </Box>

      <Card>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
            <Table size="small" sx={{ minWidth: 900 }}>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ px: 1, py: 1 }}>Execution</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Status</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Platform</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Progress</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Duration</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }}>Date</TableCell>
                  <TableCell sx={{ px: 1, py: 1 }} align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                      <Typography variant="body2" color="text.secondary">Loading reports...</Typography>
                    </TableCell>
                  </TableRow>
                ) : reports.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                      <Typography variant="body2" color="text.secondary">No reports available. Run an execution to generate a report.</Typography>
                    </TableCell>
                  </TableRow>
                ) : reports.map((r) => (
                  <TableRow key={r.id} hover sx={{ '& td': { px: 1, py: 0.75 } }}>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500} sx={{ fontSize: '0.85rem' }}>{r.name}</Typography>
                      <Typography variant="caption" color="text.secondary">{r.triggeredBy || '-'}</Typography>
                    </TableCell>
                    <TableCell><StatusBadge status={r.status} /></TableCell>
                    <TableCell>
                      <Chip label={r.platform || 'N/A'} size="small" variant="outlined" sx={{ height: 20, fontSize: '0.65rem' }} />
                    </TableCell>
                    <TableCell sx={{ minWidth: 140 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <LinearProgress variant="determinate" value={r.totalCount > 0 ? ((r.passCount + r.failCount + r.skipCount) / r.totalCount) * 100 : 0} sx={{ flex: 1, height: 4, borderRadius: 2 }} />
                        <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.65rem', whiteSpace: 'nowrap' }}>
                          {r.passCount + r.failCount + r.skipCount}/{r.totalCount}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell><Typography variant="body2" fontSize="0.8rem" color="text.secondary">{formatDuration(r.durationMs)}</Typography></TableCell>
                    <TableCell><Typography variant="body2" fontSize="0.75rem" color="text.secondary">{formatDate(r.createdAt)}</Typography></TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        <Tooltip title="View Report">
                          <IconButton size="small" onClick={() => openViewer(r.id, r.name)} sx={{ color: 'primary.main' }}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Download Report">
                          <IconButton size="small" onClick={() => downloadFile(`/executions/${r.id}/report/download`, `report_${r.id}.html`)} sx={{ color: 'success.main' }}>
                            <DownloadIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Download Logs">
                          <IconButton size="small" onClick={() => downloadFile(`/executions/${r.id}/logs/download`, `execution_${r.id}.log`)} sx={{ color: 'text.secondary' }}>
                            <DescriptionIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Email Report">
                          <IconButton size="small" onClick={() => setEmailDialog({ open: true, id: r.id, name: r.name })} sx={{ color: 'info.main' }}>
                            <EmailIcon fontSize="small" />
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

      <Dialog open={emailDialog.open} onClose={() => setEmailDialog({ open: false, id: '', name: '' })} maxWidth="sm" fullWidth>
        <DialogTitle>Email Report</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Send report for <strong>{emailDialog.name}</strong> via email.
          </Typography>
          <TextField
            autoFocus
            label="Recipient Email"
            type="email"
            fullWidth
            value={emailTo}
            onChange={(e) => setEmailTo(e.target.value)}
            placeholder="swadhinsoft@gmail.com"
            onKeyDown={(e) => e.key === 'Enter' && handleEmailSend()}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEmailDialog({ open: false, id: '', name: '' })}>Cancel</Button>
          <Button variant="contained" onClick={handleEmailSend} disabled={!emailTo.trim() || sending}>
            {sending ? 'Sending...' : 'Send'}
          </Button>
        </DialogActions>
      </Dialog>

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
            <iframe
              srcDoc={viewer.html}
              title="Report"
              style={{ width: '100%', height: '100%', border: 'none' }}
            />
          )}
        </DialogContent>
      </Dialog>

      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack(s => ({ ...s, open: false }))}>
        <Alert severity={snack.severity} variant="filled" sx={{ width: '100%' }}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  );
}
