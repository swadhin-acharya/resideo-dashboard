import { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Alert } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch {
      setError('Invalid credentials');
    }
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default' }}>
      <Card sx={{ width: 400, maxWidth: '90vw' }}>
        <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3 }}>
          <img src="/assets/resideo-logo.png" alt="Resideo" height={40} />
          <Typography variant="h5" fontWeight={700}>Test Dashboard</Typography>
          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
            {error && <Alert severity="error" sx={{ fontSize: '0.8rem' }}>{error}</Alert>}
            <TextField label="Username" size="small" fullWidth value={username} onChange={e => setUsername(e.target.value)} autoFocus />
            <TextField label="Password" type="password" size="small" fullWidth value={password} onChange={e => setPassword(e.target.value)} />
            <Button type="submit" variant="contained" fullWidth>Sign In</Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
