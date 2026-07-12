import api from './client';

export interface ReportItem {
  id: string;
  name: string;
  status: string;
  passCount: number;
  failCount: number;
  skipCount: number;
  totalCount: number;
  durationMs: number;
  platform: string;
  environment: string;
  triggeredBy: string;
  createdAt: string;
  reportPath: string;
}

export const getReports = () =>
  api.get<ReportItem[]>('/reports').then(r => r.data);

export const getReportUrl = (id: string) =>
  `${api.defaults.baseURL}/executions/${id}/report`;

export const getReportDownloadUrl = (id: string) =>
  `${api.defaults.baseURL}/executions/${id}/report/download`;

export const getLogDownloadUrl = (id: string) =>
  `${api.defaults.baseURL}/executions/${id}/logs/download`;

export const emailReport = (id: string, to: string, from?: string) =>
  api.post<{ message: string }>(`/executions/${id}/report/email`, { to, from: from || 'noreply@resideodashboard.com' }).then(r => r.data);
