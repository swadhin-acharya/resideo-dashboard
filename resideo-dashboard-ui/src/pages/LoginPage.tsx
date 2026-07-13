import { useState, useEffect } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Alert, CircularProgress } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/auth/me').catch(() => {});
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch {
      setError('Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default' }}>
      <Card sx={{ width: 400, maxWidth: '90vw' }}>
        <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
          <img src="/assets/openqa-logo.png" alt="OpenQA" height={40} />
          <Typography variant="h5" fontWeight={700}>Test Dashboard</Typography>
          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
            {error && <Alert severity="error" sx={{ fontSize: '0.8rem' }}>{error}</Alert>}
            <TextField label="Username" size="small" fullWidth value={username} onChange={e => setUsername(e.target.value)} autoFocus />
            <TextField label="Password" type="password" size="small" fullWidth value={password} onChange={e => setPassword(e.target.value)} />
            <Button type="submit" variant="contained" fullWidth disabled={loading}>{loading ? <CircularProgress size={20} sx={{ mr: 1 }} /> : null}Sign In</Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
