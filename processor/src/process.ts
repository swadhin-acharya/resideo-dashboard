import { readAllureResults } from './reader.js'
import {
  buildExecutionSummary,
  buildFeatureSummaries,
  buildTestSummaries,
  buildCategorySummaries,
} from './normalize.js'
import {
  loadHistory,
  mergeExecutionHistory,
  computeComparison,
  loadFailureHistoryState,
  mergeFailureHistory,
} from './history.js'
import { writeDashboardData, writeInternalState, type DashboardData } from './writer.js'

export interface ProcessOptions {
  /** Directory containing *-result.json, environment.properties, etc. */
  allureResultsDir: string
  /** Directory the canonical JSON is read from (history) and written to. */
  dataDir: string
  /** Defaults to the Allure executor's buildOrder, then a timestamp. */
  executionId?: string
  executionName?: string
  historyLimit?: number
  /** Skip writing to disk - used by tests that only want the computed data. */
  dryRun?: boolean
}

const DEFAULT_HISTORY_LIMIT = 50

/**
 * The single entry point that turns one allure-results directory into
 * canonical dashboard JSON, merging it into whatever history already exists
 * in dataDir. This is the one and only place Allure data becomes dashboard
 * data - both the CLI and the test suite call this same function, so there
 * is no risk of the two calculating things differently.
 */
export function processExecution(options: ProcessOptions): DashboardData {
  const historyLimit = options.historyLimit ?? DEFAULT_HISTORY_LIMIT

  const raw = readAllureResults(options.allureResultsDir)
  const executionId = options.executionId ?? String(raw.executor?.buildOrder ?? Date.now())
  const execution = buildExecutionSummary(raw, {
    executionId,
    executionName: options.executionName,
  })
  const features = buildFeatureSummaries(raw)
  const tests = buildTestSummaries(raw)
  const categories = buildCategorySummaries(raw)

  const history = loadHistory(options.dataDir)
  const mergedHistory = mergeExecutionHistory(history, execution, historyLimit)
  const comparison = computeComparison(mergedHistory.executions)

  const failureState = loadFailureHistoryState(options.dataDir)
  const mergedFailureState = mergeFailureHistory(
    failureState,
    tests,
    execution.executionId,
    execution.startTime,
  )

  const summary = {
    generatedAt: new Date().toISOString(),
    latestExecutionId: execution.executionId,
    current: execution,
    comparison,
  }

  const data: DashboardData = {
    summary,
    executions: mergedHistory.executions,
    trends: mergedHistory.trends,
    features,
    tests,
    failures: mergedFailureState.failures,
    categories,
    environment: execution.environment ?? {},
  }

  if (!options.dryRun) {
    writeDashboardData(options.dataDir, data)
    writeInternalState(options.dataDir, mergedFailureState.contributions)
  }

  return data
}
