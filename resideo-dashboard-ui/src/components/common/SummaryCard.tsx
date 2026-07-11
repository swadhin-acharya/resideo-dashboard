import { Card, CardContent, Typography, Box } from '@mui/material';
import { ReactNode } from 'react';

interface SummaryCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  color?: string;
}

export default function SummaryCard({ title, value, icon, color = 'primary.main' }: SummaryCardProps) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 2 }}>
        <Box
          sx={{
            width: 48,
            height: 48,
            borderRadius: 2,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            bgcolor: color,
            color: 'white',
            fontSize: 24,
          }}
        >
          {icon}
        </Box>
        <Box>
          <Typography variant="body2" color="text.secondary" fontWeight={500}>
            {title}
          </Typography>
          <Typography variant="h5" fontWeight={700}>
            {value}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
}
