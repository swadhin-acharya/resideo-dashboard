import { Box, Typography, TextField, MenuItem, Switch, FormControlLabel, Chip, Card, CardContent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import Grid from '@mui/material/Grid2';
import { ParameterGroup } from '../../types/config';

interface DynamicParamFormProps {
  groups: ParameterGroup[];
  values: Record<string, string>;
  additionalConfig: Record<string, string>;
  onChange: (name: string, value: string) => void;
  onAdditionalChange: (key: string, value: string) => void;
}

export default function DynamicParamForm({ groups, values, additionalConfig, onChange, onAdditionalChange }: DynamicParamFormProps) {
  const extras = Object.entries(additionalConfig);

  return (
    <Box>
      {groups.map((g) => (
        <Card key={g.group} sx={{ mb: 2 }}>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 1.5 }}>
              {g.group}
            </Typography>
            <Grid container spacing={2}>
              {g.fields.map((f) => {
                const val = values[f.name] ?? f.value ?? f.defaultValue ?? '';
                return (
                  <Grid key={f.name} size={f.type === 'boolean' ? 12 : { xs: 12, sm: 6, md: 4 }}>
                    {f.type === 'select' ? (
                      <TextField select fullWidth size="small" label={f.label} value={val}
                        onChange={(e) => onChange(f.name, e.target.value)}
                        required={f.required || false}>
                        {(f.options || []).map((o) => (
                          <MenuItem key={o} value={o}>{o}</MenuItem>
                        ))}
                      </TextField>
                    ) : f.type === 'boolean' ? (
                      <FormControlLabel
                        control={<Switch checked={val === 'true'} onChange={(e) => onChange(f.name, e.target.checked ? 'true' : 'false')} />}
                        label={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            {f.label}
                            {f.source && <Chip label={f.source} size="small" variant="outlined" sx={{ height: 18, fontSize: '0.65rem' }} />}
                          </Box>
                        }
                      />
                    ) : f.type === 'number' ? (
                      <TextField fullWidth size="small" type="number" label={f.label} value={val}
                        onChange={(e) => onChange(f.name, e.target.value)}
                        inputProps={{ min: f.min, max: f.max }} />
                    ) : (
                      <TextField fullWidth size="small" label={f.label} value={val}
                        onChange={(e) => onChange(f.name, e.target.value)}
                        placeholder={f.placeholder}
                        multiline={f.type === 'tags'}
                        minRows={f.type === 'tags' ? 1 : undefined} />
                    )}
                    {f.source && val !== '' && f.type !== 'boolean' && (
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.3 }}>
                        ← from <Chip label={f.source} size="small" variant="outlined" sx={{ height: 16, fontSize: '0.6rem', ml: 0.3 }} />
                      </Typography>
                    )}
                  </Grid>
                );
              })}
            </Grid>
          </CardContent>
        </Card>
      ))}

      {extras.length > 0 && (
        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 1 }}>
              Additional Config
              <Chip label={`${extras.length} keys`} size="small" sx={{ ml: 1 }} />
            </Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: '40%' }}>Key</TableCell>
                    <TableCell>Value</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {extras.map(([key, val]) => (
                    <TableRow key={key}>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{key}</TableCell>
                      <TableCell>
                        <TextField fullWidth size="small" value={additionalConfig[key] ?? val ?? ''}
                          onChange={(e) => onAdditionalChange(key, e.target.value)}
                          variant="standard" sx={{ minWidth: 120 }} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
