import api from './client';

export const getTrends = (days = 30) =>
  api.get<Record<string, unknown>[]>('/analytics/trends', { params: { days } }).then(r => r.data);

export const getFlakyTests = () =>
  api.get<Record<string, unknown>[]>('/analytics/flaky-tests').then(r => r.data);

export const getMostFailedFeatures = () =>
  api.get<Record<string, unknown>[]>('/analytics/most-failed/features').then(r => r.data);

export const getMostFailedScenarios = () =>
  api.get<Record<string, unknown>[]>('/analytics/most-failed/scenarios').then(r => r.data);

export const getDeviceStats = () =>
  api.get<Record<string, unknown>>('/analytics/by-device').then(r => r.data);

export const getFirmwareStats = () =>
  api.get<Record<string, unknown>>('/analytics/by-firmware').then(r => r.data);

export const compareExecutions = (left: string, right: string) =>
  api.get<Record<string, unknown>>('/analytics/comparison', { params: { left, right } }).then(r => r.data);
