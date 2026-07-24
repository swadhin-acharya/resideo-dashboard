import api from './client';
import { Device } from '../types/execution';

export const getDevices = () =>
  api.get<Device[]>('/devices').then(r => r.data);

export const getDeviceCount = () =>
  api.get<{ online: number; offline: number; total: number }>('/devices/count').then(r => r.data);
