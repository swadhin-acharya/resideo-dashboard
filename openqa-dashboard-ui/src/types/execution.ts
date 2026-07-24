export interface Execution {
  id: string;
  name: string;
  buildNumber?: string;
  branch?: string;
  platform?: string;
  environment?: string;
  executionType?: string;
  status: string;
  startTime?: string;
  endTime?: string;
  durationMs: number;
  passCount: number;
  failCount: number;
  skipCount: number;
  totalCount: number;
  reportPath?: string;
  triggerSource?: string;
  mavenCommand?: string;
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
  featureId?: string;
  scenarioName: string;
  tags?: string;
  status: string;
  durationMs: number;
  failureReason?: string;
  deviceName?: string;
}

export interface ExecutionSummary {
  id: string;
  name: string;
  status: string;
  executionType: string;
  platform: string;
  startTime: string;
  endTime: string;
  durationMs: number;
  passCount: number;
  failCount: number;
  skipCount: number;
  totalCount: number;
  triggerSource: string;
  createdAt: string;
}

export interface Device {
  id: string;
  deviceId: string;
  brand: string;
  model: string;
  osVersion: string;
  platform: string;
  status: string;
  lastSeen: string;
}

export interface ReportItem {
  id: string;
  executionId: string;
  name: string;
  filePath: string;
  fileSize: number;
  createdAt: string;
}
