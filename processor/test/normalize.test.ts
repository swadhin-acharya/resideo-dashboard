import { test } from 'node:test'
import assert from 'node:assert/strict'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { mkdtempSync, rmSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { readAllureResults } from '../src/reader.js'
import { buildExecutionSummary, buildFeatureSummaries, buildCategorySummaries, buildTestSummaries } from '../src/normalize.js'
import { processExecution } from '../src/process.js'

const __dirname = dirname(fileURLToPath(import.meta.url))
// __dirname is processor/dist/test at runtime (compiled output) - fixtures
// are plain JSON, never compiled, so they stay under the source tree.
const FIXTURES = join(__dirname, '..', '..', 'test', 'fixtures')

// Milestone 2 acceptance-test style: given a controlled Allure result
// sample, the processor must produce EXACTLY these totals. Run 3 was
// authored as 11 passed / 2 failed / 1 broken / 1 skipped out of 15.
test('buildExecutionSummary produces exact totals for the controlled run-3 fixture', () => {
  const raw = readAllureResults(join(FIXTURES, 'run-3'))
  const execution = buildExecutionSummary(raw, { executionId: '203' })

  assert.equal(execution.total, 15)
  assert.equal(execution.passed, 11)
  assert.equal(execution.failed, 2)
  assert.equal(execution.broken, 1)
  assert.equal(execution.skipped, 1)
  assert.equal(execution.unknown, 0)
  assert.equal(execution.passRate, 73.33)
  assert.equal(execution.status, 'failed') // any failed result makes the run "failed"
})

test('buildExecutionSummary reads environment.properties without fabricating missing fields', () => {
  const raw = readAllureResults(join(FIXTURES, 'run-3'))
  const execution = buildExecutionSummary(raw, { executionId: '203' })

  assert.equal(execution.environment?.os, 'Linux 6.8.0')
  assert.equal(execution.environment?.framework, 'Selenium 4 / Cucumber 7 / TestNG')
  assert.equal(execution.environment?.build, '203')
  // device was never provided anywhere in the fixture - must stay undefined,
  // never invented, so the UI can correctly show "N/A".
  assert.equal(execution.environment?.device, undefined)
})

test('buildFeatureSummaries groups by the Allure feature label and sums to the execution total', () => {
  const raw = readAllureResults(join(FIXTURES, 'run-3'))
  const features = buildFeatureSummaries(raw)

  assert.equal(features.length, 4) // Login, Inventory, Cart, Checkout
  const totalAcrossFeatures = features.reduce((sum, f) => sum + f.total, 0)
  assert.equal(totalAcrossFeatures, 15)

  const login = features.find((f) => f.name === 'Login')
  assert.ok(login)
  assert.equal(login!.total, 4)
  assert.equal(login!.passed, 3)
  assert.equal(login!.failed, 1)
})

test('buildCategorySummaries applies the sample project categories.json rules', () => {
  const raw = readAllureResults(join(FIXTURES, 'run-3'))
  const categories = buildCategorySummaries(raw)

  const assertionFailures = categories.find((c) => c.name === 'Assertion Failure')
  const elementNotFound = categories.find((c) => c.name === 'Element Not Found')

  assert.ok(assertionFailures)
  assert.equal(assertionFailures!.count, 2) // login-invalid, checkout-item-total
  assert.ok(elementNotFound)
  assert.equal(elementNotFound!.count, 1) // inventory-promo-banner

  // A category with zero matches (e.g. Timeout) must not appear at all -
  // never invent a category that has no real occurrences.
  assert.equal(categories.find((c) => c.name === 'Timeout'), undefined)
})

test('buildTestSummaries carries historyId through for later retry/flaky correlation', () => {
  const raw = readAllureResults(join(FIXTURES, 'run-3'))
  const tests = buildTestSummaries(raw)

  assert.equal(tests.length, 15)
  const invalidLogin = tests.find((t) => t.testId === 'login-invalid')
  assert.ok(invalidLogin)
  assert.equal(invalidLogin!.status, 'failed')
  assert.equal(invalidLogin!.historyId, 'login-invalid')
})

// End-to-end acceptance test in the exact style the project brief describes
// for Milestone 2: process a controlled sample, assert exact numbers appear
// in the final written summary.json - not just in an intermediate object.
test('processExecution (end-to-end, dry run) matches the controlled fixture exactly', () => {
  const tmpDataDir = mkdtempSync(join(tmpdir(), 'resideo-processor-test-'))
  try {
    const data = processExecution({
      allureResultsDir: join(FIXTURES, 'run-3'),
      dataDir: tmpDataDir,
      executionId: '203',
      dryRun: true,
    })

    assert.equal(data.summary.current.total, 15)
    assert.equal(data.summary.current.passed, 11)
    assert.equal(data.summary.current.failed, 2)
    assert.equal(data.summary.current.broken, 1)
    assert.equal(data.summary.current.skipped, 1)
    assert.equal(data.summary.latestExecutionId, '203')

    // First execution ever processed - no previous run to compare against.
    assert.equal(data.summary.comparison.passRate.available, false)
  } finally {
    rmSync(tmpDataDir, { recursive: true, force: true })
  }
})
