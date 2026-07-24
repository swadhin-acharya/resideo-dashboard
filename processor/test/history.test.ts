import { test } from 'node:test'
import assert from 'node:assert/strict'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { mkdtempSync, rmSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { processExecution } from '../src/process.js'

const __dirname = dirname(fileURLToPath(import.meta.url))
// __dirname is processor/dist/test at runtime (compiled output) - fixtures
// are plain JSON, never compiled, so they stay under the source tree.
const FIXTURES = join(__dirname, '..', '..', 'test', 'fixtures')

// Milestone 4 acceptance test: process execution A, then B, then C into the
// same data directory, and confirm history actually accumulates (not just
// overwrites) - the exact "Process A. Publish. Then process B. Dashboard
// must contain B, A." scenario from the project brief.
test('processing three executions in sequence accumulates history newest-first', () => {
  const dataDir = mkdtempSync(join(tmpdir(), 'resideo-history-test-'))
  try {
    const afterA = processExecution({
      allureResultsDir: join(FIXTURES, 'run-1'),
      dataDir,
      executionId: '201',
    })
    assert.deepEqual(
      afterA.executions.map((e) => e.executionId),
      ['201'],
    )
    assert.equal(afterA.summary.comparison.passRate.available, false)

    const afterB = processExecution({
      allureResultsDir: join(FIXTURES, 'run-2'),
      dataDir,
      executionId: '202',
    })
    assert.deepEqual(
      afterB.executions.map((e) => e.executionId),
      ['202', '201'],
    )
    // Now that a previous run exists, the KPI cards can show a real delta.
    assert.equal(afterB.summary.comparison.passRate.available, true)
    assert.equal(afterB.summary.comparison.passRate.direction, 'up') // 60% -> 66.67%

    const afterC = processExecution({
      allureResultsDir: join(FIXTURES, 'run-3'),
      dataDir,
      executionId: '203',
    })
    assert.deepEqual(
      afterC.executions.map((e) => e.executionId),
      ['203', '202', '201'],
    )
    assert.deepEqual(
      afterC.trends.map((t) => t.executionId),
      ['201', '202', '203'], // trends stay in chronological (chart) order
    )

    // Pass rate trend must be monotonically improving across the three runs,
    // matching how the fixtures were deliberately designed (60 -> 66.67 -> 73.33).
    const rates = afterC.trends.map((t) => t.passRate)
    assert.deepEqual(rates, [60, 66.67, 73.33])
  } finally {
    rmSync(dataDir, { recursive: true, force: true })
  }
})

test('historyLimit trims older executions and trend points, keeping the newest', () => {
  const dataDir = mkdtempSync(join(tmpdir(), 'resideo-history-limit-test-'))
  try {
    processExecution({ allureResultsDir: join(FIXTURES, 'run-1'), dataDir, executionId: '201', historyLimit: 2 })
    processExecution({ allureResultsDir: join(FIXTURES, 'run-2'), dataDir, executionId: '202', historyLimit: 2 })
    const afterC = processExecution({
      allureResultsDir: join(FIXTURES, 'run-3'),
      dataDir,
      executionId: '203',
      historyLimit: 2,
    })

    assert.equal(afterC.executions.length, 2)
    assert.deepEqual(
      afterC.executions.map((e) => e.executionId),
      ['203', '202'], // run-1 fell off the back of the retained window
    )
    assert.equal(afterC.trends.length, 2)
  } finally {
    rmSync(dataDir, { recursive: true, force: true })
  }
})

test('reprocessing the same executionId replaces it rather than duplicating it', () => {
  const dataDir = mkdtempSync(join(tmpdir(), 'resideo-history-idempotent-test-'))
  try {
    processExecution({ allureResultsDir: join(FIXTURES, 'run-1'), dataDir, executionId: '201' })
    const reprocessed = processExecution({ allureResultsDir: join(FIXTURES, 'run-1'), dataDir, executionId: '201' })

    assert.equal(reprocessed.executions.length, 1)
    assert.equal(reprocessed.trends.length, 1)
  } finally {
    rmSync(dataDir, { recursive: true, force: true })
  }
})

test('reprocessing the same execution does not inflate top-failure occurrence counts', () => {
  const dataDir = mkdtempSync(join(tmpdir(), 'resideo-failures-idempotent-test-'))
  try {
    processExecution({ allureResultsDir: join(FIXTURES, 'run-1'), dataDir, executionId: '201' })
    const first = processExecution({ allureResultsDir: join(FIXTURES, 'run-1'), dataDir, executionId: '201' })
    const invalidLoginFirst = first.failures.find((f) => f.testId === 'login-invalid')
    assert.equal(invalidLoginFirst?.occurrences, 1)

    // A genuinely new execution where the same test fails again SHOULD
    // increment its occurrence count.
    const second = processExecution({ allureResultsDir: join(FIXTURES, 'run-2'), dataDir, executionId: '202' })
    const invalidLoginSecond = second.failures.find((f) => f.testId === 'login-invalid')
    assert.equal(invalidLoginSecond?.occurrences, 2)
  } finally {
    rmSync(dataDir, { recursive: true, force: true })
  }
})
