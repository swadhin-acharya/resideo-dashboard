import api from './client';
import { ReportItem } from '../types/execution';

export const getReports = () =>
  api.get<ReportItem[]>('/reports').then(r => r.data);

export const getReportByExecution = (executionId: string) =>
  api.get<ReportItem[]>(`/reports/by-execution/${executionId}`).then(r => r.data);
