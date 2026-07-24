/**
 * Canonical data model for ResideoNextGen Dashboard.
 *
 * These interfaces describe the STATIC JSON produced at build time by the
 * Allure result processor (public/data/*.json). They are normalized DTOs
 * derived from Allure structured result data — Allure remains the single
 * source of truth. React components must only visualize these shapes and
 * must never independently recompute totals, pass rates, or health scores.
 */

export type TestStatus = 'passed' | 'failed' | 'broken' | 'skipped' | 'unknown'

export interface StatusCounts {
  total: number
  passed: number
  failed: number
  broken: number
  skipped: number
  unknown: number
}

export interface ComparisonMetric {
  /** Delta vs. the previous execution, already computed by the processor. */
  value: number
  direction: 'up' | 'down' | 'flat'
  /** True only when a previous execution exists to compare against. */
  available: boolean
}

export interface ExecutionSummary extends StatusCounts {
  executionId: string
  executionName: string
  status: TestStatus | 'mixed'

  startTime: string
  endTime: string
  duration: number // milliseconds

  passRate: number // 0-100

  environment?: EnvironmentInfo
  executor?: ExecutorInfo
}

export interface ExecutorInfo {
  name?: string
  type?: string
  buildName?: string
  buildOrder?: number
  branch?: string
  url?: string
}

export interface EnvironmentInfo {
  os?: string
  java?: string
  platform?: string
  browser?: string
  framework?: string
  branch?: string
  build?: string
  machine?: string
  device?: DeviceInfo
}

export interface DeviceInfo {
  deviceId?: string
  brand?: string
  model?: string
  androidVersion?: string
}

export interface FeatureSummary extends StatusCounts {
  featureId: string
  name: string
  passRate: number
}

export interface TestSummary extends StatusCounts {
  testId: string
  name: string
  feature: string
  status: TestStatus
  duration: number
  severity?: string
  historyId?: string
  retries?: number
}

export interface StepResult {
  name: string
  status: TestStatus
  duration: number
  steps?: StepResult[]
  error?: string
  stackTrace?: string
  attachments?: AttachmentRef[]
}

export interface AttachmentRef {
  name: string
  type: string
  source: string
}

export interface FailureSummary {
  testId: string
  name: string
  feature: string
  occurrences: number
  lastSeen: string
}

export interface CategorySummary {
  name: string
  count: number
  matchedStatuses?: TestStatus[]
}

export interface TrendPoint {
  executionId: string
  date: string
  total: number
  passed: number
  failed: number
  broken: number
  skipped: number
  passRate: number
  duration: number // milliseconds
}

export interface RecentExecutionRow extends StatusCounts {
  executionId: string
  status: TestStatus | 'mixed'
  passRate: number
  duration: number
  date: string
}

export interface SummaryData {
  generatedAt: string
  latestExecutionId: string
  current: ExecutionSummary
  comparison: {
    passed: ComparisonMetric
    failed: ComparisonMetric
    passRate: ComparisonMetric
    duration: ComparisonMetric
    total: ComparisonMetric
  }
}
