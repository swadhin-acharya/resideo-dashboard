export interface Execution {
  id: string;
  name: string;
  buildNumber: string;
  triggeredBy: string;
  branch: string;
  commitHash: string;
  platform: string;
  environment: string;
  firmwareVersion: string;
  appVersion: string;
  executionType: string;
  cucumberTags: string;
  featurePaths: string;
  mavenCommand: string;
  status: string;
  startTime: string;
  endTime: string;
  durationMs: number;
  passCount: number;
  failCount: number;
  skipCount: number;
  totalCount: number;
  reportPath: string;
  logPath: string;
  features?: FeatureSummary[];
  scenarios?: ScenarioInfo[];
  createdAt: string;
}

export interface FeatureSummary {
  id: string;
  featureName: string;
  status: string;
  durationMs: number;
  passCount: number;
  failCount: number;
  skipCount: number;
}

export interface ScenarioInfo {
  id: string;
  featureId: string;
  scenarioName: string;
  tags: string;
  status: string;
  durationMs: number;
  failureReason: string;
  deviceName: string;
}

export interface FeatureFile {
  path: string;
  name: string;
  fullPath: string;
}

export interface ExecutionSummary {
  totalExecutions: number;
  passed: number;
  failed: number;
  aborted: number;
  running: number;
  passRate: number;
  totalScenarios: number;
  totalPassed: number;
  totalFailed: number;
  totalSkipped: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface WsMessage {
  type: string;
  executionId: string;
  data: Record<string, unknown>;
  timestamp: number;
}
