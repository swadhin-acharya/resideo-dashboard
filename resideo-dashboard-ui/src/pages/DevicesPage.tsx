import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Chip, Button, Dialog, DialogTitle,
  DialogContent, TextField, DialogActions, MenuItem,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddIcon from '@mui/icons-material/Add';
import AndroidIcon from '@mui/icons-material/Android';
import AppleIcon from '@mui/icons-material/Apple';
import { getDevices, createDevice, reserveDevice, releaseDevice } from '../api/devices';
import { Device } from '../types/device';

const STATUS_COLORS: Record<string, 'success' | 'error' | 'warning' | 'info'> = {
  AVAILABLE: 'success',
  BUSY: 'error',
  OFFLINE: 'warning',
  RESERVED: 'info',
};

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [newDevice, setNewDevice] = useState({ name: '', platform: 'ANDROID', udid: '', osVersion: '' });

  const load = () => getDevices().then(setDevices);
  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    await createDevice(newDevice);
    setDialogOpen(false);
    load();
  };

  const handleReserve = async (id: string) => {
    await reserveDevice(id, 'current-user');
    load();
  };

  const handleRelease = async (id: string) => {
    await releaseDevice(id);
    load();
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Devices</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
          Add Device
        </Button>
      </Box>

      <Grid container spacing={2}>
        {devices.map((d) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={d.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  {d.platform === 'ANDROID' ? <AndroidIcon color="success" /> : <AppleIcon />}
                  <Typography variant="h6">{d.name}</Typography>
                  <Chip label={d.status} size="small"
                    color={STATUS_COLORS[d.status] || 'default'} sx={{ ml: 'auto' }} />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {d.platform} · {d.osVersion || '-'} · {d.udid?.slice(0, 20) || '-'}
                </Typography>
                <Box sx={{ mt: 1.5, display: 'flex', gap: 1 }}>
                  {(d.status === 'AVAILABLE' || d.status === 'OFFLINE') && (
                    <Button size="small" variant="outlined" onClick={() => handleReserve(d.id)}>
                      Reserve
                    </Button>
                  )}
                  {d.status === 'RESERVED' && (
                    <Button size="small" variant="outlined" color="warning" onClick={() => handleRelease(d.id)}>
                      Release
                    </Button>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
        {devices.length === 0 && (
          <Grid size={12}>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              No devices registered. Add one to get started.
            </Typography>
          </Grid>
        )}
      </Grid>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Register Device</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Name" value={newDevice.name}
            onChange={(e) => setNewDevice({ ...newDevice, name: e.target.value })}
            sx={{ mb: 2, mt: 1 }} />
          <TextField select fullWidth label="Platform" value={newDevice.platform}
            onChange={(e) => setNewDevice({ ...newDevice, platform: e.target.value })}
            sx={{ mb: 2 }}>
            <MenuItem value="ANDROID">Android</MenuItem>
            <MenuItem value="IOS">iOS</MenuItem>
          </TextField>
          <TextField fullWidth label="UDID" value={newDevice.udid}
            onChange={(e) => setNewDevice({ ...newDevice, udid: e.target.value })}
            sx={{ mb: 2 }} />
          <TextField fullWidth label="OS Version" value={newDevice.osVersion}
            onChange={(e) => setNewDevice({ ...newDevice, osVersion: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate}>Add</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
