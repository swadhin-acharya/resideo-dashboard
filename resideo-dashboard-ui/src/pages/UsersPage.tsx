import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Select, MenuItem, FormControl, InputLabel,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { getUsers, createUser, updateUser, deleteUser, UserResponse } from '../api/auth';

const ROLES = ['PLATFORM_ADMIN', 'USER'];

export default function UsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [open, setOpen] = useState(false);
  const [edit, setEdit] = useState<UserResponse | null>(null);
  const [form, setForm] = useState({ username: '', email: '', password: '', globalRole: 'USER' });

  const load = async () => { try { setUsers(await getUsers()); } catch {} };

  useEffect(() => { load(); }, []);

  const handleSave = async () => {
    try {
      if (edit) {
        await updateUser(edit.id, { email: form.email, displayName: form.username, globalRole: form.globalRole });
      } else {
        await createUser(form);
      }
      setOpen(false);
      load();
    } catch {}
  };

  const handleDelete = async (id: string) => {
    try { await deleteUser(id); load(); } catch {}
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Users</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEdit(null); setForm({ username: '', email: '', password: '', globalRole: 'USER' }); setOpen(true); }}>
          Add User
        </Button>
      </Box>
      <Card>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Username</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Last Login</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map(u => (
                  <TableRow key={u.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>{u.displayName || u.username}</Typography>
                    </TableCell>
                    <TableCell><Typography variant="body2" color="text.secondary">{u.email}</Typography></TableCell>
                    <TableCell><Chip label={u.globalRole} size="small" color={u.globalRole === 'PLATFORM_ADMIN' ? 'primary' : 'default'} sx={{ height: 20, fontSize: '0.65rem' }} /></TableCell>
                    <TableCell><Chip label={u.enabled ? 'Active' : 'Disabled'} size="small" color={u.enabled ? 'success' : 'default'} sx={{ height: 20, fontSize: '0.65rem' }} /></TableCell>
                    <TableCell><Typography variant="caption" color="text.secondary">{u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString() : '—'}</Typography></TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => { setEdit(u); setForm({ username: u.displayName || u.username, email: u.email, password: '', globalRole: u.globalRole }); setOpen(true); }}>
                        <EditIcon sx={{ fontSize: 16 }} />
                      </IconButton>
                      <IconButton size="small" color="error" onClick={() => handleDelete(u.id)}>
                        <DeleteIcon sx={{ fontSize: 16 }} />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{edit ? 'Edit User' : 'Add User'}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField label="Username" size="small" value={form.username} onChange={e => setForm(f => ({ ...f, username: e.target.value }))} disabled={!!edit} />
          <TextField label="Email" size="small" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
          {!edit && <TextField label="Password" type="password" size="small" value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />}
          <FormControl size="small">
            <InputLabel>Role</InputLabel>
            <Select value={form.globalRole} label="Role" onChange={e => setForm(f => ({ ...f, globalRole: e.target.value }))}>
              {ROLES.map(r => <MenuItem key={r} value={r}>{r}</MenuItem>)}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>{edit ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
