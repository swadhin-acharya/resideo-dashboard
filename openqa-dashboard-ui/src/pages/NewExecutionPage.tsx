import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Typography, Box, TextField, MenuItem, Button, Card, CardContent, Alert,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { createExecution } from '../api/executions';

export default function NewExecutionPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    platform: 'ANDROID',
    environment: 'QA',
    branch: 'main',
    buildNumber: '',
    executionType: 'REGRESSION',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [field]: e.target.value });
  };

  const handleSubmit = async () => {
    setError('');
    setLoading(true);
    try {
      const res = await createExecution({
        name: form.name || `Execution ${new Date().toLocaleString()}`,
        platform: form.platform,
        environment: form.environment,
        branch: form.branch,
        buildNumber: form.buildNumber,
        executionType: form.executionType,
      });
      navigate(`/executions/${res.id}`);
    } catch {
      setError('Failed to start execution');
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>New Execution</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ maxWidth: 600 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid size={12}>
              <TextField fullWidth label="Execution Name" value={form.name}
                onChange={handleChange('name')} placeholder="Leave blank for auto-generated" />
            </Grid>
            <Grid size={6}>
              <TextField select fullWidth label="Execution Type" value={form.executionType}
                onChange={handleChange('executionType')}>
                <MenuItem value="REGRESSION">Regression</MenuItem>
                <MenuItem value="SMOKE">Smoke</MenuItem>
                <MenuItem value="SANITY">Sanity</MenuItem>
              </TextField>
            </Grid>
            <Grid size={6}>
              <TextField select fullWidth label="Platform" value={form.platform}
                onChange={handleChange('platform')}>
                <MenuItem value="ANDROID">Android</MenuItem>
              </TextField>
            </Grid>
            <Grid size={6}>
              <TextField select fullWidth label="Environment" value={form.environment}
                onChange={handleChange('environment')}>
                <MenuItem value="QA">QA</MenuItem>
                <MenuItem value="STAGING">Staging</MenuItem>
              </TextField>
            </Grid>
            <Grid size={6}>
              <TextField fullWidth label="Branch" value={form.branch}
                onChange={handleChange('branch')} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth label="Build Number" value={form.buildNumber}
                onChange={handleChange('buildNumber')} />
            </Grid>
          </Grid>

          <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
            <Button variant="contained" size="large" startIcon={<PlayArrowIcon />}
              onClick={handleSubmit} disabled={loading}>
              {loading ? 'Starting...' : 'Start Execution'}
            </Button>
            <Button variant="outlined" onClick={() => navigate('/executions')}>
              Cancel
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
