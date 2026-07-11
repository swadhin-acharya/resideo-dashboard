import { Box, Chip, Typography, Card, CardContent, Tooltip } from '@mui/material';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import CodeIcon from '@mui/icons-material/Code';
import SettingsIcon from '@mui/icons-material/Settings';
import DescriptionIcon from '@mui/icons-material/Description';
import { ConfigFileInfo } from '../../types/config';

const EXT_ICONS: Record<string, typeof CodeIcon> = {
  '.properties': SettingsIcon,
  '.yml': CodeIcon,
  '.yaml': CodeIcon,
  '.json': DescriptionIcon,
  '.env': InsertDriveFileIcon,
};

const EXT_COLORS: Record<string, string> = {
  '.properties': '#1976d2',
  '.yml': '#388e3c',
  '.yaml': '#388e3c',
  '.json': '#f57c00',
  '.env': '#7b1fa2',
};

interface ConfigFilePickerProps {
  files: ConfigFileInfo[];
  selectedPath: string;
  onSelect: (path: string) => void;
}

export default function ConfigFilePicker({ files, selectedPath, onSelect }: ConfigFilePickerProps) {
  if (files.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ py: 1 }}>
        No config files found in workspace. Create a <code>dashboard-config.json</code>, <code>application.properties</code>, or similar.
      </Typography>
    );
  }

  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
      {files.map((f) => {
        const Icon = EXT_ICONS[f.extension] || InsertDriveFileIcon;
        const color = EXT_COLORS[f.extension] || '#757575';
        const selected = f.path === selectedPath;
        return (
          <Tooltip key={f.path} title={`${f.path} (${(f.size / 1024).toFixed(1)} KB)`}>
            <Chip
              icon={<Icon sx={{ fontSize: 18, color: selected ? '#fff' : color }} />}
              label={f.fileName}
              variant={selected ? 'filled' : 'outlined'}
              onClick={() => onSelect(f.path)}
              sx={{
                borderColor: color,
                bgcolor: selected ? color : 'transparent',
                color: selected ? '#fff' : 'text.primary',
                fontWeight: selected ? 600 : 400,
                '&:hover': { bgcolor: selected ? color : `${color}15` },
              }}
            />
          </Tooltip>
        );
      })}
    </Box>
  );
}
