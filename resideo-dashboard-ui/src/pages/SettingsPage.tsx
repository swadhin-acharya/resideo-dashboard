import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Stack, Divider,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import {
  getProjects, createProject, archiveProject, getProjectMembers,
  addProjectMember, removeProjectMember,
  getUsers, ProjectResponse, UserResponse,
} from '../api/auth';

const PROJECT_ROLES = [
  { value: 'PROJECT_ADMIN', label: 'Project Admin' },
  { value: 'MANAGER', label: 'Manager' },
  { value: 'AUTOMATION_ENGINEER', label: 'Automation Engg' },
  { value: 'AUTOMATION_LEAD', label: 'Automation Lead Engg' },
  { value: 'MANUAL_QA', label: 'Manual QA' },
  { value: 'LAB_ACCOUNT', label: 'Lab Account' },
];

interface Member {
  userId: string;
  role: string;
  username?: string;
  email?: string;
  displayName?: string;
}

export default function SettingsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [members, setMembers] = useState<Member[]>([]);

  const [projDialogOpen, setProjDialogOpen] = useState(false);
  const [projForm, setProjForm] = useState({ name: '', slug: '', description: '' });

  const [memberDialogOpen, setMemberDialogOpen] = useState(false);
  const [memberForm, setMemberForm] = useState({ userId: '', role: 'AUTOMATION_ENGINEER' });

  const loadProjects = async () => {
    try {
      const p = await getProjects();
      setProjects(p);
    } catch {}
  };

  const loadUsers = async () => {
    try { setUsers(await getUsers()); } catch {}
  };

  const loadMembers = async (projectId: string) => {
    try { setMembers(await getProjectMembers(projectId)); } catch { setMembers([]); }
  };

  useEffect(() => { loadProjects(); loadUsers(); }, []);

  useEffect(() => {
    if (selectedProjectId) loadMembers(selectedProjectId);
    else setMembers([]);
  }, [selectedProjectId]);

  const selectedProject = projects.find(p => p.id === selectedProjectId);

  const handleCreateProject = async () => {
    try {
      await createProject(projForm);
      setProjDialogOpen(false);
      setProjForm({ name: '', slug: '', description: '' });
      loadProjects();
    } catch {}
  };

  const handleArchiveProject = async (id: string) => {
    try {
      await archiveProject(id);
      if (selectedProjectId === id) setSelectedProjectId(null);
      loadProjects();
    } catch {}
  };

  const handleAddMember = async () => {
    if (!selectedProjectId || !memberForm.userId) return;
    try {
      await addProjectMember(selectedProjectId, memberForm.userId, memberForm.role);
      setMemberDialogOpen(false);
      setMemberForm({ userId: '', role: 'AUTOMATION_ENGINEER' });
      loadMembers(selectedProjectId);
    } catch {}
  };

  const handleRemoveMember = async (userId: string) => {
    if (!selectedProjectId) return;
    try {
      await removeProjectMember(selectedProjectId, userId);
      loadMembers(selectedProjectId);
    } catch {}
  };

  const availableUsers = users.filter(
    u => !members.some(m => m.userId === u.id)
  );

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 2 }}>
        System Settings
      </Typography>
      <Box sx={{ display: 'flex', gap: 2, height: 'calc(100vh - 140px)' }}>
        <Card sx={{ width: 300, flexShrink: 0, display: 'flex', flexDirection: 'column' }}>
          <CardContent sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="subtitle2" fontWeight={600}>Projects</Typography>
            <Button size="small" variant="contained" startIcon={<AddIcon />}
              onClick={() => { setProjForm({ name: '', slug: '', description: '' }); setProjDialogOpen(true); }}>
              New
            </Button>
          </CardContent>
          <Box sx={{ overflow: 'auto', flex: 1 }}>
            {projects.map(p => (
              <Box
                key={p.id}
                onClick={() => setSelectedProjectId(p.id)}
                sx={{
                  px: 2, py: 1.5, cursor: 'pointer', borderBottom: 1, borderColor: 'divider',
                  bgcolor: selectedProjectId === p.id ? 'primary.main' : 'transparent',
                  color: selectedProjectId === p.id ? '#fff' : 'text.primary',
                  '&:hover': { bgcolor: selectedProjectId === p.id ? 'primary.dark' : 'action.hover' },
                }}
              >
                <Typography variant="body2" fontWeight={600}>{p.name}</Typography>
                <Typography variant="caption" color={selectedProjectId === p.id ? '#ccc' : 'text.secondary'}>
                  {p.slug}
                </Typography>
              </Box>
            ))}
            {projects.length === 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                No projects yet
              </Typography>
            )}
          </Box>
        </Card>

        <Card sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          {selectedProject ? (
            <CardContent sx={{ p: 3, overflow: 'auto', flex: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
                <Box>
                  <Typography variant="h5" fontWeight={700}>{selectedProject.name}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    {selectedProject.description || 'No description'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Slug: {selectedProject.slug} &middot; Created: {new Date(selectedProject.createdAt).toLocaleDateString()}
                  </Typography>
                </Box>
                <Button size="small" color="error" variant="outlined"
                  onClick={() => handleArchiveProject(selectedProject.id)}>
                  Archive
                </Button>
              </Box>

              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="subtitle1" fontWeight={600}>Team Members</Typography>
                <Button size="small" variant="contained" startIcon={<PersonAddIcon />}
                  onClick={() => { setMemberForm({ userId: '', role: 'AUTOMATION_ENGINEER' }); setMemberDialogOpen(true); }}
                  disabled={availableUsers.length === 0}>
                  Add Member
                </Button>
              </Box>

              <TableContainer component={Paper} variant="outlined" sx={{ bgcolor: 'transparent', border: 'none' }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>User</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Role</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {members.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={4} align="center">
                          <Typography variant="body2" color="text.secondary" sx={{ py: 3 }}>
                            No members assigned to this project
                          </Typography>
                        </TableCell>
                      </TableRow>
                    )}
                    {members.map(m => {
                      const roleDef = PROJECT_ROLES.find(r => r.value === m.role);
                      return (
                        <TableRow key={m.userId} hover>
                          <TableCell>
                            <Typography variant="body2" fontWeight={600}>
                              {m.displayName || m.username || m.userId}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" color="text.secondary">
                              {m.email || '—'}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={roleDef?.label || m.role}
                              size="small"
                              color={m.role === 'PROJECT_ADMIN' ? 'primary' : 'default'}
                              variant={m.role === 'PROJECT_ADMIN' ? 'filled' : 'outlined'}
                              sx={{ height: 20, fontSize: '0.65rem' }}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <IconButton size="small" color="error"
                              onClick={() => handleRemoveMember(m.userId)}>
                              <DeleteIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          ) : (
            <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flex: 1 }}>
              <Typography variant="body1" color="text.secondary">
                Select a project to manage its members
              </Typography>
            </CardContent>
          )}
        </Card>
      </Box>

      <Dialog open={projDialogOpen} onClose={() => setProjDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Project</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField label="Name" size="small" value={projForm.name}
            onChange={e => setProjForm(f => ({ ...f, name: e.target.value }))} />
          <TextField label="Slug" size="small" value={projForm.slug}
            helperText="URL-friendly identifier (e.g. 'denali')"
            onChange={e => setProjForm(f => ({ ...f, slug: e.target.value }))} />
          <TextField label="Description" size="small" multiline rows={2} value={projForm.description}
            onChange={e => setProjForm(f => ({ ...f, description: e.target.value }))} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setProjDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateProject}
            disabled={!projForm.name || !projForm.slug}>Create</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={memberDialogOpen} onClose={() => setMemberDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Team Member — {selectedProject?.name}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <Stack spacing={1}>
            <Typography variant="caption" fontWeight={600} color="text.secondary">User</Typography>
            {availableUsers.length === 0 ? (
              <Typography variant="body2" color="text.secondary">All users are already members of this project</Typography>
            ) : (
              availableUsers.map(u => (
                <Box
                  key={u.id}
                  onClick={() => setMemberForm(f => ({ ...f, userId: u.id }))}
                  sx={{
                    p: 1.5, borderRadius: 1, border: 1, cursor: 'pointer',
                    borderColor: memberForm.userId === u.id ? 'primary.main' : 'divider',
                    bgcolor: memberForm.userId === u.id ? 'primary.main' : 'transparent',
                    color: memberForm.userId === u.id ? '#fff' : 'text.primary',
                    '&:hover': { borderColor: 'primary.light' },
                  }}
                >
                  <Typography variant="body2" fontWeight={600}>{u.displayName || u.username}</Typography>
                  <Typography variant="caption" color={memberForm.userId === u.id ? '#ccc' : 'text.secondary'}>
                    {u.email} — {u.globalRole}
                  </Typography>
                </Box>
              ))
            )}
          </Stack>
          <Stack spacing={1}>
            <Typography variant="caption" fontWeight={600} color="text.secondary">Role</Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
              {PROJECT_ROLES.map(r => (
                <Chip
                  key={r.value}
                  label={r.label}
                  size="small"
                  onClick={() => setMemberForm(f => ({ ...f, role: r.value }))}
                  color={memberForm.role === r.value ? 'primary' : 'default'}
                  variant={memberForm.role === r.value ? 'filled' : 'outlined'}
                  sx={{ cursor: 'pointer' }}
                />
              ))}
            </Box>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMemberDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleAddMember}
            disabled={!memberForm.userId}>Add</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
