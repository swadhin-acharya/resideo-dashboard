import api from './client';
import { Execution, ExecutionSummary, PagedResponse, FeatureFile } from '../types/execution';

export const getExecutions = (params?: Record<string, string | number>) =>
  api.get<PagedResponse<Execution>>('/executions', { params }).then(r => r.data);

export const getExecution = (id: string) =>
  api.get<Execution>(`/executions/${id}`).then(r => r.data);

export const createExecution = (data: Record<string, unknown>) =>
  api.post<Execution>('/executions', data).then(r => r.data);

export const triggerExecution = (data: Record<string, unknown>) =>
  api.post<Execution>('/executions/trigger', data).then(r => r.data);

export const updateStatus = (id: string, status: string) =>
  api.patch<Execution>(`/executions/${id}/status`, { status }).then(r => r.data);

export const updateName = (id: string, name: string) =>
  api.patch<Execution>(`/executions/${id}/name`, { name }).then(r => r.data);

export const deleteExecution = (id: string) =>
  api.delete(`/executions/${id}`);

export const cancelExecution = (id: string) =>
  api.post(`/executions/${id}/cancel`);

export const getSummary = () =>
  api.get<ExecutionSummary>('/executions/summary').then(r => r.data);

export const getRunning = () =>
  api.get<Execution[]>('/executions/running').then(r => r.data);

export const getFeatureFiles = () =>
  api.get<FeatureFile[]>('/executions/feature-files').then(r => r.data);
