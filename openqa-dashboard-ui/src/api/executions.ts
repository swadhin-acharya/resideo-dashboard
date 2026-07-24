import api from './client';
import { ExecutionSummary, Execution } from '../types/execution';

export const getExecutions = () =>
  api.get<ExecutionSummary[]>('/executions').then(r => r.data);

export const getExecution = (id: string) =>
  api.get<Execution>(`/executions/${id}`).then(r => r.data);

export const createExecution = (data: Record<string, unknown>) =>
  api.post<ExecutionSummary>('/executions', data).then(r => r.data);

export const deleteExecution = (id: string) =>
  api.delete(`/executions/${id}`);

export const updateStatus = (id: string, status: string) =>
  api.patch(`/executions/${id}/status`, { status });

export const finishExecution = (id: string, status?: string) =>
  api.post(`/executions/${id}/finish`, status ? { status } : {});

export const addFeature = (executionId: string, data: Record<string, unknown>) =>
  api.post(`/executions/${executionId}/features`, data).then(r => r.data);

export const addScenario = (executionId: string, data: Record<string, unknown>) =>
  api.post(`/executions/${executionId}/scenarios`, data).then(r => r.data);

export const addStep = (executionId: string, data: Record<string, unknown>) =>
  api.post(`/executions/${executionId}/steps`, data).then(r => r.data);

export const addLog = (executionId: string, data: Record<string, unknown>) =>
  api.post(`/executions/${executionId}/logs`, data).then(r => r.data);
