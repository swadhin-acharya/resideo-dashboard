import api from './client';
import { Device, Thermostat } from '../types/device';

export const getDevices = () =>
  api.get<Device[]>('/devices').then(r => r.data);

export const createDevice = (data: Record<string, unknown>) =>
  api.post<Device>('/devices', data).then(r => r.data);

export const reserveDevice = (id: string, reservedBy: string) =>
  api.post<Device>(`/devices/${id}/reserve`, { reservedBy }).then(r => r.data);

export const releaseDevice = (id: string) =>
  api.post<Device>(`/devices/${id}/release`).then(r => r.data);

export const getThermostats = () =>
  api.get<Thermostat[]>('/thermostats').then(r => r.data);
