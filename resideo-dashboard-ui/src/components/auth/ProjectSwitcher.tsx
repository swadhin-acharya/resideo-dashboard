import { useState } from 'react';
import { Button, Menu, MenuItem, Typography, Box } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import { useAuth } from '../../context/AuthContext';
import { getProjects, ProjectResponse } from '../../api/auth';

export default function ProjectSwitcher() {
  const { user, activeProjectId, setProjectId } = useAuth();
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleOpen = async (e: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(e.currentTarget);
    if (projects.length === 0) {
      try {
        const data = await getProjects();
        const filtered = data.filter(p => p.slug !== 'default');
        setProjects(filtered);
        if (activeProjectId && !filtered.some(p => p.id === activeProjectId) && filtered.length > 0) {
          setProjectId(filtered[0].id);
        }
      } catch {}
    }
  };

  const activeProject = projects.find(p => p.id === activeProjectId);

  if (!user) return null;

  return (
    <>
      <Button
        size="small"
        onClick={handleOpen}
        endIcon={<KeyboardArrowDownIcon sx={{ fontSize: 16 }} />}
        sx={{ textTransform: 'none', color: 'text.primary', fontSize: '0.8rem', fontWeight: 500 }}
      >
        {activeProject?.name || 'Select Project'}
      </Button>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)} slotProps={{ paper: { sx: { minWidth: 200 } } }}>
        {projects.map(p => (
          <MenuItem key={p.id} selected={p.id === activeProjectId} onClick={() => { setProjectId(p.id); setAnchorEl(null); }}>
            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
              <Typography variant="body2" fontWeight={600}>{p.name}</Typography>
              <Typography variant="caption" color="text.secondary">{p.slug}</Typography>
            </Box>
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}
