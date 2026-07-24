import { processExecution } from './process.js'

function parseArgs(argv: string[]): Record<string, string> {
  const args: Record<string, string> = {}
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i]
    if (!arg.startsWith('--')) continue
    const key = arg.slice(2)
    const next = argv[i + 1]
    if (next && !next.startsWith('--')) {
      args[key] = next
      i++
    } else {
      args[key] = 'true'
    }
  }
  return args
}

function main() {
  const args = parseArgs(process.argv.slice(2))

  const allureResultsDir = args['allure-results'] ?? 'allure-results'
  const dataDir = args['out'] ?? 'public/data'
  const historyLimit = args['history-limit'] ? Number(args['history-limit']) : undefined
  const executionId = args['execution-id']
  const executionName = args['execution-name']

  console.log(`[processor] Reading Allure results from: ${allureResultsDir}`)
  console.log(`[processor] Writing dashboard data to:   ${dataDir}`)

  const data = processExecution({
    allureResultsDir,
    dataDir,
    executionId,
    executionName,
    historyLimit,
  })

  const c = data.summary.current
  console.log(
    `[processor] Execution #${c.executionId}: total=${c.total} passed=${c.passed} failed=${c.failed} ` +
      `broken=${c.broken} skipped=${c.skipped} passRate=${c.passRate}% duration=${c.duration}ms`,
  )
  console.log(`[processor] History now has ${data.executions.length} execution(s), ${data.trends.length} trend point(s).`)
}

main()
