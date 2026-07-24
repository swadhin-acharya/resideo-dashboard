import { mkdirSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import type {
  SummaryData,
  RecentExecutionRow,
  TrendPoint,
  FeatureSummary,
  TestSummary,
  FailureSummary,
  CategorySummary,
  EnvironmentInfo,
} from './models.js'

export interface DashboardData {
  summary: SummaryData
  executions: RecentExecutionRow[]
  trends: TrendPoint[]
  features: FeatureSummary[]
  tests: TestSummary[]
  failures: FailureSummary[]
  categories: CategorySummary[]
  environment: EnvironmentInfo
}

/** Writes the canonical JSON files the React dashboard fetches at runtime
 * (public/data/*.json - see project brief section 7). */
export function writeDashboardData(dataDir: string, data: DashboardData): void {
  mkdirSync(dataDir, { recursive: true })

  const write = (fileName: string, value: unknown) =>
    writeFileSync(join(dataDir, fileName), JSON.stringify(value, null, 2) + '\n', 'utf-8')

  write('summary.json', data.summary)
  write('executions.json', data.executions)
  write('trends.json', data.trends)
  write('features.json', data.features)
  write('tests.json', data.tests)
  write('failures.json', data.failures)
  write('categories.json', data.categories)
  write('environment.json', data.environment ?? {})
}

/** Internal-only bookkeeping (not fetched by the dashboard) that lets
 * reprocessing be idempotent. See history.ts for why this exists. */
export function writeInternalState(dataDir: string, contributions: Record<string, string[]>): void {
  mkdirSync(dataDir, { recursive: true })
  writeFileSync(
    join(dataDir, '.failures-contributions.json'),
    JSON.stringify(contributions, null, 2) + '\n',
    'utf-8',
  )
}
