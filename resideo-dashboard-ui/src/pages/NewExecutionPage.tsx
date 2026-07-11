import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Typography, Box, TextField, MenuItem, Button, Card, CardContent, Alert,
  Chip, Autocomplete, FormControlLabel, Switch,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { triggerExecution, getFeatureFiles } from '../api/executions';
import { getAvailableConfigFiles, parseConfigFile } from '../api/config';
import { FeatureFile } from '../types/execution';
import { ConfigFileInfo, MergedConfig, ParameterGroup } from '../types/config';
import ConfigFilePicker from '../components/config/ConfigFilePicker';
import DynamicParamForm from '../components/config/DynamicParamForm';

export default function NewExecutionPage() {
  const navigate = useNavigate();
  const [configFiles, setConfigFiles] = useState<ConfigFileInfo[]>([]);
  const [selectedConfigPath, setSelectedConfigPath] = useState('');
  const [mergedConfig, setMergedConfig] = useState<MergedConfig | null>(null);
  const [featureFiles, setFeatureFiles] = useState<FeatureFile[]>([]);

  const [paramValues, setParamValues] = useState<Record<string, string>>({});
  const [additionalValues, setAdditionalValues] = useState<Record<string, string>>({});

  const [form, setForm] = useState({
    name: '',
    executionType: 'REGRESSION',
    cucumberTags: '',
    selectedFeatures: [] as string[],
    platform: 'ANDROID',
    environment: 'QA',
    firmwareVersion: '1.3605.1550',
    appVersion: '3.0.0',
    mavenProfile: 'android',
    parallel: false,
    retryCount: 0,
    triggeredBy: '',
    buildNumber: '',
    branch: 'main',
    jvmParams: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getAvailableConfigFiles().then(setConfigFiles).catch(() => {});
    getFeatureFiles().then(setFeatureFiles).catch(() => {});
  }, []);

  const handleSelectConfig = useCallback(async (path: string) => {
    if (path === selectedConfigPath) {
      setSelectedConfigPath('');
      setMergedConfig(null);
      return;
    }
    setSelectedConfigPath(path);
    try {
      const merged = await parseConfigFile(path);
      setMergedConfig(merged);

      const newParams: Record<string, string> = {};
      if (merged.groups) {
        for (const g of merged.groups) {
          for (const f of g.fields) {
            newParams[f.name] = f.value ?? f.defaultValue ?? '';
          }
        }
      }
      setParamValues(newParams);
      setAdditionalValues({ ...merged.additionalConfig });
    } catch {
      setMergedConfig(null);
    }
  }, [selectedConfigPath]);

  const handleParamChange = (name: string, value: string) => {
    setParamValues(prev => ({ ...prev, [name]: value }));
  };

  const handleAdditionalChange = (key: string, value: string) => {
    setAdditionalValues(prev => ({ ...prev, [key]: value }));
  };

  const usingConfig = mergedConfig !== null;

  const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [field]: e.target.value });
  };

  const buildMavenCmd = () => {
    if (usingConfig) {
      const parts: string[] = [];
      if (mergedConfig!.groups) {
        for (const g of mergedConfig!.groups) {
          if (g.fields) {
            for (const f of g.fields) {
              const val = paramValues[f.name] ?? f.defaultValue ?? '';
              if (!val) continue;
              if (f.type === 'boolean') {
                if (val === 'true' && f.mapIfTrue) parts.push(f.mapIfTrue.replace('{value}', val));
              } else {
                if (f.map) parts.push(f.map.replace('{value}', val));
              }
            }
          }
        }
      }
      for (const [k, v] of Object.entries(additionalValues)) {
        if (v) parts.push(`-D${k}=${v}`);
      }
      const template = mergedConfig!.commandTemplate || 'mvn test {params}';
      return template.replace('{params}', parts.join(' '));
    }
    return `mvn test \\
  ${form.cucumberTags ? `-Dcucumber.filter.tags="${form.cucumberTags}" \\` : ''}
  ${form.selectedFeatures.length > 0 ? `-Dcucumber.features="${form.selectedFeatures.join(',')}" \\` : ''}
  -P${form.mavenProfile} \\
  -Dplatform=${form.platform.toLowerCase()} \\
  -Denvironment=${form.environment} \\
  -Dfirmware.version=${form.firmwareVersion} \\
  -Dapp.version=${form.appVersion} \\
  ${form.parallel ? '-Dparallel=true \\' : ''}
  ${form.retryCount > 0 ? `-DretryCount=${form.retryCount} \\` : ''}
  ${form.jvmParams || ''}`;
  };

  const handleSubmit = async () => {
    setError('');
    setLoading(true);
    try {
      if (usingConfig) {
        const res = await triggerExecution({
          name: form.name || `Execution ${new Date().toLocaleString()}`,
          triggeredBy: form.triggeredBy,
          buildNumber: form.buildNumber,
          branch: form.branch,
          mavenCommand: mavenCmd,
          additionalConfig: additionalValues,
        });
        navigate(`/executions/${res.id}`);
      } else {
        const res = await triggerExecution({
          name: form.name || `Execution ${new Date().toLocaleString()}`,
          executionType: form.executionType,
          cucumberTags: form.cucumberTags,
          featurePaths: form.selectedFeatures,
          platform: form.platform,
          environment: form.environment,
          firmwareVersion: form.firmwareVersion,
          appVersion: form.appVersion,
          mavenProfile: form.mavenProfile,
          parallel: form.parallel,
          retryCount: Number(form.retryCount),
          triggeredBy: form.triggeredBy,
          buildNumber: form.buildNumber,
          branch: form.branch,
          jvmParams: form.jvmParams,
          additionalConfig: additionalValues,
        });
        navigate(`/executions/${res.id}`);
      }
    } catch (err) {
      setError('Failed to trigger execution');
      setLoading(false);
    }
  };

  const mavenCmd = buildMavenCmd();

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>New Execution</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 1.5 }}>Execution Name</Typography>
              <TextField fullWidth label="Execution Name" value={form.name}
                onChange={handleChange('name')}
                placeholder="Leave blank for auto-generated name" />
            </CardContent>
          </Card>

          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 1.5 }}>
                Config Files
                {selectedConfigPath && (
                  <Chip label="Loaded" size="small" color="success" sx={{ ml: 1 }} />
                )}
              </Typography>
              <ConfigFilePicker
                files={configFiles}
                selectedPath={selectedConfigPath}
                onSelect={handleSelectConfig}
              />
            </CardContent>
          </Card>

          {usingConfig ? (
            <DynamicParamForm
              groups={mergedConfig!.groups || []}
              values={paramValues}
              additionalConfig={additionalValues}
              onChange={handleParamChange}
              onAdditionalChange={handleAdditionalChange}
            />
          ) : (
            <>
              {selectedConfigPath ? (
                <Alert severity="info" sx={{ mb: 2 }}>
                  Config file selected but no <code>dashboard-config.json</code> found. Showing all config keys as editable below.
                </Alert>
              ) : (
                <Alert severity="info" sx={{ mb: 2 }}>
                  No config file selected. Showing default fields. Select a config file above to auto-fill parameters.
                </Alert>
              )}

              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Grid container spacing={2}>
                    <Grid size={6}>
                      <TextField select fullWidth label="Execution Type" value={form.executionType}
                        onChange={handleChange('executionType')}>
                        <MenuItem value="SMOKE">Smoke Suite</MenuItem>
                        <MenuItem value="REGRESSION">Regression Suite</MenuItem>
                        <MenuItem value="SANITY">Sanity Suite</MenuItem>
                        <MenuItem value="TAG">Cucumber Tags</MenuItem>
                        <MenuItem value="FAILED">Failed Scenarios</MenuItem>
                      </TextField>
                    </Grid>
                    <Grid size={6}>
                      <TextField select fullWidth label="Platform" value={form.platform}
                        onChange={handleChange('platform')}>
                        <MenuItem value="ANDROID">Android</MenuItem>
                        <MenuItem value="IOS">iOS</MenuItem>
                      </TextField>
                    </Grid>
                    <Grid size={12}>
                      <Autocomplete
                        multiple
                        options={featureFiles}
                        getOptionLabel={(o) => o.path}
                        value={featureFiles.filter(f => form.selectedFeatures.includes(f.path))}
                        onChange={(_, newVal) => setForm({ ...form, selectedFeatures: newVal.map(v => v.path) })}
                        renderInput={(params) => (
                          <TextField {...params} label="Feature Files" placeholder="Choose features..." />
                        )}
                        renderTags={(tagValue, getTagProps) =>
                          tagValue.map((option, index) => (
                            <Chip label={option.name} size="small" {...getTagProps({ index })} key={option.path} />
                          ))
                        }
                      />
                    </Grid>
                    <Grid size={12}>
                      <TextField fullWidth label="Cucumber Tags" value={form.cucumberTags}
                        onChange={handleChange('cucumberTags')} placeholder="@smoke,@regression,@P0" />
                    </Grid>
                    <Grid size={6}>
                      <TextField select fullWidth label="Environment" value={form.environment}
                        onChange={handleChange('environment')}>
                        <MenuItem value="QA">QA</MenuItem>
                        <MenuItem value="STAGING">Staging</MenuItem>
                        <MenuItem value="PRODUCTION">Production</MenuItem>
                      </TextField>
                    </Grid>
                    <Grid size={6}>
                      <TextField select fullWidth label="Maven Profile" value={form.mavenProfile}
                        onChange={handleChange('mavenProfile')}>
                        <MenuItem value="android">Android</MenuItem>
                        <MenuItem value="ios">iOS</MenuItem>
                        <MenuItem value="parallel">Parallel</MenuItem>
                      </TextField>
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="Firmware Version" value={form.firmwareVersion}
                        onChange={handleChange('firmwareVersion')} />
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="App Version" value={form.appVersion}
                        onChange={handleChange('appVersion')} />
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="Build Number" value={form.buildNumber}
                        onChange={handleChange('buildNumber')} />
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="Triggered By" value={form.triggeredBy}
                        onChange={handleChange('triggeredBy')} />
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="Branch" value={form.branch}
                        onChange={handleChange('branch')} />
                    </Grid>
                    <Grid size={4}>
                      <TextField fullWidth label="Retry Count" type="number" value={form.retryCount}
                        onChange={handleChange('retryCount')} />
                    </Grid>
                    <Grid size={6}>
                      <FormControlLabel
                        control={<Switch checked={form.parallel} onChange={(e) => setForm({ ...form, parallel: e.target.checked })} />}
                        label="Parallel Execution"
                      />
                    </Grid>
                    <Grid size={12}>
                      <TextField fullWidth label="JVM Params" value={form.jvmParams}
                        onChange={handleChange('jvmParams')} placeholder="-Xmx2g -XX:+UseG1GC" />
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </>
          )}

          <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
            <Button variant="contained" size="large" startIcon={<PlayArrowIcon />}
              onClick={handleSubmit} disabled={loading}>
              {loading ? 'Starting...' : 'Start Execution'}
            </Button>
            <Button variant="outlined" onClick={() => navigate('/executions')}>
              Cancel
            </Button>
          </Box>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ bgcolor: '#f5f5f5', position: 'sticky', top: 80 }}>
            <CardContent>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>Maven Command Preview</Typography>
              <Box
                component="pre"
                sx={{
                  fontSize: '0.75rem',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                  bgcolor: '#1e1e1e',
                  color: '#d4d4d4',
                  p: 2,
                  borderRadius: 1,
                  overflow: 'auto',
                  maxHeight: 400,
                }}
              >
                {mavenCmd}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
