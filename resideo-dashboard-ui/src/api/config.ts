import api from './client';
import { ConfigFileInfo, MergedConfig } from '../types/config';

export const getAvailableConfigFiles = () =>
  api.get<ConfigFileInfo[]>('/config/available-files').then(r => r.data);

export const parseConfigFile = (filePath: string) =>
  api.post<MergedConfig>('/config/parse', { filePath }).then(r => r.data);

export const previewCommand = (params: {
  paramValues: Record<string, string>;
  additionalValues: Record<string, string>;
  commandTemplate: string;
}) =>
  api.post<{ command: string }>('/config/preview', params).then(r => r.data);
