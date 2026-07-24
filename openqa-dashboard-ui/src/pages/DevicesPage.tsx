import { useEffect, useState } from 'react';
import { Typography, Box, Card, CardContent, Chip } from '@mui/material';
import Grid from '@mui/material/Grid2';
import AndroidIcon from '@mui/icons-material/Android';
import { getDevices } from '../api/devices';
import { Device } from '../types/execution';

const STATUS_COLORS: Record<string, 'success' | 'error' | 'warning' | 'info'> = {
  ONLINE: 'success',
  OFFLINE: 'warning',
};

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);

  useEffect(() => { getDevices().then(setDevices); }, []);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Devices</Typography>
      </Box>

      <Grid container spacing={2}>
        {devices.length === 0 ? (
          <Grid size={12}>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              No devices detected. Connect an Android device via ADB.
            </Typography>
          </Grid>
        ) : devices.map((d) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={d.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <AndroidIcon color="success" />
                  <Typography variant="h6">{d.brand || 'Unknown'} {d.model || ''}</Typography>
                  <Chip label={d.status} size="small"
                    color={STATUS_COLORS[d.status] || 'default'} sx={{ ml: 'auto' }} />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {d.osVersion || '-'} · {d.deviceId?.slice(0, 20) || '-'}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
