import api from './client';

export const getTrends = (days = 30) =>
  api.get<Record<string, unknown>[]>('/analytics/trends', { params: { days } }).then(r => r.data);

export const getDeviceStats = () =>
  api.get<Record<string, unknown>>('/analytics/device-stats').then(r => r.data);
