import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Box, Button, Chip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { getExecutions } from '../api/executions';
import { ExecutionSummary } from '../types/execution';
import StatusBadge from '../components/common/StatusBadge';

export default function ExecutionsPage() {
  const [data, setData] = useState<ExecutionSummary[]>([]);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const searchQ = searchParams.get('q')?.toLowerCase() || '';

  useEffect(() => {
    getExecutions().then(setData);
  }, []);

  const filtered = useMemo(() => {
    if (!searchQ) return data;
    return data.filter(e => e.name?.toLowerCase().includes(searchQ));
  }, [data, searchQ]);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Execution History</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/executions/new')}>
          New Execution
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Platform</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Passed</TableCell>
              <TableCell>Failed</TableCell>
              <TableCell>Duration</TableCell>
              <TableCell>Date</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                  <Typography variant="body2" color="text.secondary">{searchQ ? 'No executions match your search.' : 'No executions yet.'}</Typography>
                </TableCell>
              </TableRow>
            ) : filtered.map((row) => (
              <TableRow key={row.id} hover sx={{ cursor: 'pointer' }}
                onClick={() => navigate(`/executions/${row.id}`)}>
                <TableCell>
                  <Typography variant="body2" fontWeight={500}>{row.name}</Typography>
                </TableCell>
                <TableCell>{row.executionType || '-'}</TableCell>
                <TableCell>
                  <Chip label={row.platform || 'N/A'} size="small" variant="outlined" />
                </TableCell>
                <TableCell><StatusBadge status={row.status} /></TableCell>
                <TableCell>{row.passCount}</TableCell>
                <TableCell>{row.failCount}</TableCell>
                <TableCell>
                  {row.durationMs ? `${(row.durationMs / 1000).toFixed(1)}s` : '-'}
                </TableCell>
                <TableCell>
                  {row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '-'}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
