import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, TablePagination, Box, Button, TextField, MenuItem,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { getExecutions } from '../api/executions';
import { Execution } from '../types/execution';
import StatusBadge from '../components/common/StatusBadge';

export default function ExecutionsPage() {
  const [data, setData] = useState<Execution[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [total, setTotal] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [platformFilter, setPlatformFilter] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const params: Record<string, string | number> = { page, size: rowsPerPage };
    if (statusFilter) params.status = statusFilter;
    if (platformFilter) params.platform = platformFilter;
    getExecutions(params).then((r) => {
      setData(r.content);
      setTotal(r.totalElements);
    });
  }, [page, rowsPerPage, statusFilter, platformFilter]);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Execution History</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/executions/new')}>
          New Execution
        </Button>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <TextField select label="Status" size="small" value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          sx={{ minWidth: 140 }}>
          <MenuItem value="">All</MenuItem>
          <MenuItem value="PASSED">Passed</MenuItem>
          <MenuItem value="FAILED">Failed</MenuItem>
          <MenuItem value="RUNNING">Running</MenuItem>
          <MenuItem value="PENDING">Pending</MenuItem>
        </TextField>
        <TextField select label="Platform" size="small" value={platformFilter}
          onChange={(e) => { setPlatformFilter(e.target.value); setPage(0); }}
          sx={{ minWidth: 140 }}>
          <MenuItem value="">All</MenuItem>
          <MenuItem value="ANDROID">Android</MenuItem>
          <MenuItem value="IOS">iOS</MenuItem>
        </TextField>
      </Box>

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Build</TableCell>
              <TableCell>Platform</TableCell>
              <TableCell>Environment</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Passed</TableCell>
              <TableCell>Failed</TableCell>
              <TableCell>Duration</TableCell>
              <TableCell>Triggered</TableCell>
              <TableCell>Date</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row) => (
              <TableRow key={row.id} hover sx={{ cursor: 'pointer' }}
                onClick={() => navigate(`/executions/${row.id}`)}>
                <TableCell>{row.buildNumber || '-'}</TableCell>
                <TableCell>{row.platform}</TableCell>
                <TableCell>{row.environment || '-'}</TableCell>
                <TableCell><StatusBadge status={row.status} /></TableCell>
                <TableCell>{row.passCount}</TableCell>
                <TableCell>{row.failCount}</TableCell>
                <TableCell>
                  {row.durationMs ? `${(row.durationMs / 1000).toFixed(1)}s` : '-'}
                </TableCell>
                <TableCell>{row.triggeredBy || '-'}</TableCell>
                <TableCell>
                  {row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '-'}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
        />
      </TableContainer>
    </Box>
  );
}
