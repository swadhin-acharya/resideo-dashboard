import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Snackbar, Alert, Tooltip, Stack, ToggleButtonGroup, ToggleButton,
} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { getApiTokens, createApiToken, updateApiToken, revokeApiToken } from '../api/auth';
import { useAuth } from '../context/AuthContext';

interface ApiToken {
  id: string;
  name: string;
  purpose: string | null;
  tokenPrefix: string;
  enabled: boolean;
  lastUsedAt: string | null;
  expiresAt: string | null;
  createdAt: string;
  fullToken: string | null;
}

const EXPIRY_OPTIONS = [
  { label: '1 Day', days: 1 },
  { label: '15 Days', days: 15 },
  { label: '30 Days', days: 30 },
  { label: '3 Months', days: 90 },
  { label: '6 Months', days: 180 },
  { label: '1 Year', days: 365 },
];

export default function ApiKeysPage() {
  const { activeProjectId } = useAuth();
  const [tokens, setTokens] = useState<ApiToken[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [tokenName, setTokenName] = useState('');
  const [tokenPurpose, setTokenPurpose] = useState('');
  const [expiryDays, setExpiryDays] = useState<number>(-1);
  const [creating, setCreating] = useState(false);
  const [newTokenValue, setNewTokenValue] = useState<string | null>(null);
  const [editDialog, setEditDialog] = useState<{ open: boolean; id: string; name: string; purpose: string }>(
    { open: false, id: '', name: '', purpose: '' }
  );
  const [snack, setSnack] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>(
    { open: false, message: '', severity: 'success' }
  );

  const load = async () => {
    try {
      const data = await getApiTokens();
      setTokens(data as ApiToken[]);
    } catch { setTokens([]); }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    if (!tokenName.trim()) return;
    setCreating(true);
    try {
      const body: { name: string; purpose?: string; projectId?: string; expiresInDays?: number } = {
        name: tokenName.trim(),
      };
      if (tokenPurpose.trim()) body.purpose = tokenPurpose.trim();
      if (activeProjectId) body.projectId = activeProjectId;
      if (expiryDays > 0) body.expiresInDays = expiryDays;
      const result = await createApiToken(body) as ApiToken;
      if (result.fullToken) {
        setNewTokenValue(result.fullToken);
      } else {
        setSnack({ open: true, message: 'Token created', severity: 'success' });
      }
      setCreateOpen(false);
      setTokenName('');
      setTokenPurpose('');
      setExpiryDays(-1);
      load();
    } catch {
      setSnack({ open: true, message: 'Failed to create token', severity: 'error' });
    }
    setCreating(false);
  };

  const handleEdit = async () => {
    if (!editDialog.name.trim() && !editDialog.purpose.trim()) return;
    try {
      await updateApiToken(editDialog.id, {
        name: editDialog.name.trim() || undefined,
        purpose: editDialog.purpose.trim() || undefined,
      });
      setSnack({ open: true, message: 'Token updated', severity: 'success' });
      setEditDialog({ open: false, id: '', name: '', purpose: '' });
      load();
    } catch {
      setSnack({ open: true, message: 'Failed to update token', severity: 'error' });
    }
  };

  const handleRevoke = async (id: string) => {
    try {
      await revokeApiToken(id);
      setSnack({ open: true, message: 'Token revoked', severity: 'success' });
      load();
    } catch {
      setSnack({ open: true, message: 'Failed to revoke token', severity: 'error' });
    }
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text).then(() => {
      setSnack({ open: true, message: label + ' copied', severity: 'success' });
    }).catch(() => {
      setSnack({ open: true, message: 'Failed to copy', severity: 'error' });
    });
  };

  const userTokens = tokens.filter(t => t.name !== 'web-session');

  const formatExpiry = (expiresAt: string | null): string => {
    if (!expiresAt) return 'Never';
    const exp = new Date(expiresAt);
    const now = new Date();
    if (exp < now) return 'Expired';
    const days = Math.ceil((exp.getTime() - now.getTime()) / 86400000);
    return `${days}d (${exp.toLocaleDateString()})`;
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>API Keys</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>
          New Token
        </Button>
      </Box>

      {activeProjectId && (
        <Card sx={{ mb: 2 }}>
          <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
            <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 1 }}>
              Active Project ID
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box
                sx={{
                  fontFamily: 'monospace', fontSize: '0.8rem', bgcolor: 'action.hover',
                  px: 1.5, py: 0.75, borderRadius: 1, border: 1, borderColor: 'divider',
                  flex: 1, overflow: 'hidden', textOverflow: 'ellipsis',
                }}
              >
                {activeProjectId}
              </Box>
              <Tooltip title="Copy Project ID">
                <IconButton size="small" onClick={() => copyToClipboard(activeProjectId, 'Project ID')}>
                  <ContentCopyIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Box>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
          {userTokens.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
              No API tokens yet. Create one to use with the Cucumber plugin or CI/CD.
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ px: 1, py: 1 }}>Name</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Purpose</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Prefix</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Status</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Expires</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Last Used</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }}>Created</TableCell>
                    <TableCell sx={{ px: 1, py: 1 }} align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {userTokens.map((t) => (
                    <TableRow key={t.id} hover sx={{ '& td': { px: 1, py: 0.75 } }}>
                      <TableCell>
                        <Typography variant="body2" fontWeight={500}>{t.name}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontSize="0.75rem" color="text.secondary" sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {t.purpose || '-'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace" fontSize="0.75rem" color="text.secondary">
                          {t.tokenPrefix}...
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={t.enabled ? 'Active' : 'Revoked'}
                          size="small"
                          color={t.enabled ? 'success' : 'default'}
                          variant="outlined"
                          sx={{ height: 20, fontSize: '0.65rem' }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontSize="0.75rem" color={t.expiresAt && new Date(t.expiresAt) < new Date() ? 'error.main' : 'text.secondary'}>
                          {formatExpiry(t.expiresAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontSize="0.75rem" color="text.secondary">
                          {t.lastUsedAt ? new Date(t.lastUsedAt).toLocaleString() : 'Never'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontSize="0.75rem" color="text.secondary">
                          {new Date(t.createdAt).toLocaleDateString()}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                          {t.enabled && (
                            <>
                              <Tooltip title="Edit">
                                <IconButton size="small" onClick={() => setEditDialog({ open: true, id: t.id, name: t.name, purpose: t.purpose || '' })}>
                                  <EditIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Revoke">
                                <IconButton size="small" color="error" onClick={() => handleRevoke(t.id)}>
                                  <DeleteIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                            </>
                          )}
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create API Token</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Stack spacing={2}>
            <TextField
              label="Token Name"
              size="small"
              fullWidth
              value={tokenName}
              onChange={(e) => setTokenName(e.target.value)}
              placeholder="e.g. ci-token, cucumber-plugin"
            />
            <TextField
              label="Purpose"
              size="small"
              fullWidth
              value={tokenPurpose}
              onChange={(e) => setTokenPurpose(e.target.value)}
              placeholder="e.g. Used for CI/CD pipeline in GitHub Actions"
              multiline
              minRows={2}
              maxRows={4}
            />
            <Box>
              <Typography variant="caption" fontWeight={600} color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                Expires In
              </Typography>
              <ToggleButtonGroup
                size="small"
                value={expiryDays}
                exclusive
                onChange={(_, val) => val !== null && setExpiryDays(val)}
                sx={{ flexWrap: 'wrap', gap: 0.5 }}
              >
                <ToggleButton value={-1} sx={{ fontSize: '0.7rem', px: 1 }}>Never</ToggleButton>
                {EXPIRY_OPTIONS.map(opt => (
                  <ToggleButton key={opt.days} value={opt.days} sx={{ fontSize: '0.7rem', px: 1 }}>
                    {opt.label}
                  </ToggleButton>
                ))}
              </ToggleButtonGroup>
            </Box>
            {activeProjectId && (
              <Typography variant="caption" color="text.secondary">
                Token will be scoped to the active project
              </Typography>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!tokenName.trim() || creating}>
            {creating ? 'Creating...' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!newTokenValue} onClose={() => setNewTokenValue(null)} maxWidth="md" fullWidth>
        <DialogTitle>Token Created</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="warning.main" sx={{ mb: 2, fontWeight: 600 }}>
            Copy this token now. You won't be able to see it again.
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                fontFamily: 'monospace', fontSize: '0.8rem', bgcolor: 'action.hover',
                px: 1.5, py: 1, borderRadius: 1, border: 1, borderColor: 'divider',
                flex: 1, wordBreak: 'break-all',
              }}
            >
              {newTokenValue}
            </Box>
            <Tooltip title="Copy Token">
              <IconButton
                color="primary"
                onClick={() => {
                  if (newTokenValue) copyToClipboard(newTokenValue, 'Token');
                }}
              >
                <ContentCopyIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button variant="contained" onClick={() => setNewTokenValue(null)}>Done</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={editDialog.open} onClose={() => setEditDialog({ open: false, id: '', name: '', purpose: '' })} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Token</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Stack spacing={2}>
            <TextField
              label="Token Name"
              size="small"
              fullWidth
              value={editDialog.name}
              onChange={(e) => setEditDialog(d => ({ ...d, name: e.target.value }))}
            />
            <TextField
              label="Purpose"
              size="small"
              fullWidth
              value={editDialog.purpose}
              onChange={(e) => setEditDialog(d => ({ ...d, purpose: e.target.value }))}
              multiline
              minRows={2}
              maxRows={4}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialog({ open: false, id: '', name: '', purpose: '' })}>Cancel</Button>
          <Button variant="contained" onClick={handleEdit} disabled={!editDialog.name.trim() && !editDialog.purpose.trim()}>Save</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snack.open} autoHideDuration={3000} onClose={() => setSnack(s => ({ ...s, open: false }))}>
        <Alert severity={snack.severity} variant="filled" sx={{ width: '100%' }}>{snack.message}</Alert>
      </Snackbar>
    </Box>
  );
}
