import { Chip } from '@mui/material';

const COLORS: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
  PASSED: 'success',
  FAILED: 'error',
  SKIPPED: 'warning',
  RUNNING: 'info',
  PENDING: 'default',
  ABORTED: 'error',
};

export default function StatusBadge({ status }: { status: string }) {
  const s = status?.toUpperCase() || '';
  return (
    <Chip
      label={s}
      size="small"
      color={COLORS[s] || 'default'}
      variant="filled"
      sx={{ fontWeight: 600, fontSize: '0.75rem' }}
    />
  );
}
