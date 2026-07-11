import { useEffect, useState } from 'react';
import {
  Typography, Box, Card, CardContent, Chip,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import DeviceThermostatIcon from '@mui/icons-material/DeviceThermostat';
import { getThermostats } from '../api/devices';
import { Thermostat } from '../types/device';

export default function ThermostatsPage() {
  const [thermostats, setThermostats] = useState<Thermostat[]>([]);

  useEffect(() => { getThermostats().then(setThermostats); }, []);

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>Thermostats</Typography>

      <Grid container spacing={2}>
        {thermostats.map((t) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={t.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <DeviceThermostatIcon color="primary" />
                  <Typography variant="h6">{t.name || t.serialPort || 'Unnamed'}</Typography>
                  <Chip label={t.status} size="small"
                    color={t.status === 'ONLINE' ? 'success' : 'error'} sx={{ ml: 'auto' }} />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Port: {t.serialPort || '-'} · FW: {t.firmwareVersion || '-'}
                </Typography>
                {t.reservedBy && (
                  <Typography variant="body2" color="text.secondary">
                    Reserved by: {t.reservedBy}
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
        {thermostats.length === 0 && (
          <Grid size={12}>
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              No thermostats registered.
            </Typography>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
