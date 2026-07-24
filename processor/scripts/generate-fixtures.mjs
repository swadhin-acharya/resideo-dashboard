// Generates a controlled, hand-designed set of Allure result fixtures
// (real Allure2 JSON schema: {uuid}-result.json, {uuid}-container.json,
// environment.properties, categories.json, executor.json) for exactly the
// 15 scenarios written in ../../saucedemo-selenium-sample. Three runs are
// generated (oldest -> newest) with a deliberate mix of passed/failed/
// broken/skipped that shifts between runs, so the processor's history-merge
// logic (Milestone 4) has real trend movement to work with, not just one
// static snapshot.
//
// This is a one-time authoring aid, not part of the production build. Real
// projects generate allure-results by actually running their test suite.
//
// Usage: node scripts/generate-fixtures.mjs

import { mkdirSync, writeFileSync, rmSync, existsSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { randomUUID } from 'node:crypto'

const __dirname = dirname(fileURLToPath(import.meta.url))
const FIXTURES_ROOT = join(__dirname, '..', 'test', 'fixtures')

const SCENARIOS = [
  { id: 'login-standard', feature: 'Login', name: 'Successful login with the standard user', severity: 'critical', tags: ['smoke'] },
  { id: 'login-problem', feature: 'Login', name: 'Successful login with the problem user', severity: 'normal', tags: [] },
  { id: 'login-locked-out', feature: 'Login', name: 'Locked out user sees an error message', severity: 'normal', tags: [] },
  { id: 'login-invalid', feature: 'Login', name: 'Invalid credentials are rejected', severity: 'normal', tags: ['bug'] },
  { id: 'inventory-count', feature: 'Inventory', name: 'Inventory page lists six products', severity: 'normal', tags: [] },
  { id: 'inventory-sort', feature: 'Inventory', name: 'Sort products by price low to high', severity: 'minor', tags: [] },
  { id: 'inventory-add-to-cart', feature: 'Inventory', name: 'Add item to cart updates the cart badge', severity: 'critical', tags: [] },
  { id: 'inventory-promo-banner', feature: 'Inventory', name: 'Promo banner is visible on the inventory page', severity: 'minor', tags: ['bug'] },
  { id: 'cart-add-multiple', feature: 'Cart', name: 'Add multiple items and verify cart count', severity: 'normal', tags: [] },
  { id: 'cart-remove', feature: 'Cart', name: 'Remove item from cart', severity: 'normal', tags: [] },
  { id: 'cart-badge-empty', feature: 'Cart', name: 'Cart badge disappears when the cart is empty', severity: 'minor', tags: [] },
  { id: 'checkout-valid', feature: 'Checkout', name: 'Complete checkout with valid information', severity: 'blocker', tags: [] },
  { id: 'checkout-empty-first-name', feature: 'Checkout', name: 'Checkout fails with an empty first name', severity: 'normal', tags: [] },
  { id: 'checkout-item-total', feature: 'Checkout', name: 'Checkout item total matches expected price', severity: 'critical', tags: ['bug'] },
  { id: 'checkout-glitch-user', feature: 'Checkout', name: 'Checkout succeeds for the performance glitch user', severity: 'normal', tags: [] },
]

const EXCEPTIONS = {
  assertion: {
    message: 'AssertionError: expected values to be equal',
    trace: 'java.lang.AssertionError: expected [true] but found [false]\n\tat org.testng.Assert.fail(Assert.java:96)\n\tat org.testng.Assert.failNotEquals(Assert.java:1050)\n\tat com.resideo.sample.stepdefinitions.CheckoutSteps.the_item_total_should_equal(CheckoutSteps.java:41)',
  },
  notFound: {
    message: 'org.openqa.selenium.NoSuchElementException: no such element: Unable to locate element: {"method":"css selector","selector":"[data-test=\'promo-banner\']"}',
    trace: 'org.openqa.selenium.NoSuchElementException: no such element: Unable to locate element\n\tat org.openqa.selenium.remote.RemoteWebDriver.findElement(RemoteWebDriver.java:415)\n\tat com.resideo.sample.pages.InventoryPage.isPromoBannerVisible(InventoryPage.java:58)',
  },
  stale: {
    message: 'org.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document',
    trace: 'org.openqa.selenium.StaleElementReferenceException: stale element reference\n\tat org.openqa.selenium.remote.RemoteWebElement.getText(RemoteWebElement.java:102)\n\tat com.resideo.sample.pages.InventoryPage.removeFromCart(InventoryPage.java:63)',
  },
  pending: {
    message: 'io.cucumber.java.PendingException: Cart-clearing helper step not implemented yet',
    trace: 'io.cucumber.java.PendingException: Cart-clearing helper step not implemented yet\n\tat com.resideo.sample.stepdefinitions.CartSteps.the_cart_clearing_workflow_is_not_yet_implemented(CartSteps.java:29)',
  },
}

// Per-run status overrides, keyed by scenario id. Anything not listed here
// defaults to "passed". Ordered oldest -> newest to show real trend
// movement (60% -> 66.67% -> 73.33% pass rate).
const RUNS = [
  {
    key: 'run-1',
    executionId: '201',
    date: '2026-07-22T09:00:00Z',
    buildNumber: 201,
    overrides: {
      'login-locked-out': { status: 'failed', exception: 'assertion' },
      'login-invalid': { status: 'failed', exception: 'assertion' },
      'inventory-promo-banner': { status: 'broken', exception: 'notFound' },
      'cart-badge-empty': { status: 'skipped', exception: 'pending' },
      'checkout-item-total': { status: 'failed', exception: 'assertion' },
      'checkout-empty-first-name': { status: 'broken', exception: 'stale' },
    },
  },
  {
    key: 'run-2',
    executionId: '202',
    date: '2026-07-23T09:00:00Z',
    buildNumber: 202,
    overrides: {
      'login-invalid': { status: 'failed', exception: 'assertion' },
      'inventory-sort': { status: 'failed', exception: 'assertion' },
      'inventory-promo-banner': { status: 'broken', exception: 'notFound' },
      'cart-remove': { status: 'broken', exception: 'stale' },
      'cart-badge-empty': { status: 'skipped', exception: 'pending' },
    },
  },
  {
    key: 'run-3',
    executionId: '203',
    date: '2026-07-24T09:00:00Z',
    buildNumber: 203,
    overrides: {
      'login-invalid': { status: 'failed', exception: 'assertion' },
      'inventory-promo-banner': { status: 'broken', exception: 'notFound' },
      'cart-badge-empty': { status: 'skipped', exception: 'pending' },
      'checkout-item-total': { status: 'failed', exception: 'assertion' },
    },
  },
]

function slug(text) {
  return text.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')
}

function buildResult(scenario, run, startMs, durationMs) {
  const override = run.overrides[scenario.id]
  const status = override?.status ?? 'passed'
  const exceptionKey = override?.exception
  const exception = exceptionKey ? EXCEPTIONS[exceptionKey] : null

  const stepStatus = (stepIndex, totalSteps) => {
    // The failing/broken/skipped step is always the last one; earlier
    // steps passed (mirrors how a real scenario fails mid-way through).
    if (!exception) return 'passed'
    return stepIndex === totalSteps - 1 ? status : 'passed'
  }

  const stepNames = [
    `Given the user is on a precondition for "${scenario.name}"`,
    `When the user performs the action under test`,
    `Then the expected outcome is verified`,
  ]

  let cursor = startMs
  const stepDuration = Math.floor(durationMs / stepNames.length)
  const steps = stepNames.map((name, index) => {
    const start = cursor
    const stop = start + stepDuration
    cursor = stop
    const thisStatus = stepStatus(index, stepNames.length)
    const step = { name, status: thisStatus, stage: 'finished', start, stop, steps: [], attachments: [], parameters: [] }
    if (thisStatus !== 'passed' && exception) {
      step.statusDetails = { known: false, muted: false, flaky: false, message: exception.message, trace: exception.trace }
    }
    return step
  })

  const uuid = randomUUID()
  const result = {
    uuid,
    historyId: scenario.id,
    testCaseId: scenario.id,
    fullName: `com.resideo.sample.${scenario.feature.toLowerCase()}.${slug(scenario.name)}`,
    name: scenario.name,
    status,
    stage: 'finished',
    start: startMs,
    stop: startMs + durationMs,
    labels: [
      { name: 'feature', value: scenario.feature },
      { name: 'suite', value: scenario.feature },
      { name: 'severity', value: scenario.severity },
      { name: 'framework', value: 'cucumber' },
      { name: 'language', value: 'java' },
      { name: 'host', value: 'ci-runner-04' },
      { name: 'thread', value: 'main' },
      ...scenario.tags.map((tag) => ({ name: 'tag', value: tag })),
    ],
    links: [],
    parameters: [],
    steps,
    attachments: [],
  }

  if (status !== 'passed' && exception) {
    result.statusDetails = { known: false, muted: false, flaky: false, message: exception.message, trace: exception.trace }
  }

  return result
}

function writeEnvironmentProperties(dir, run) {
  const lines = [
    'OS=Linux 6.8.0',
    'Java=17.0.11',
    'Platform=Web',
    'Browser=Chrome (headless)',
    'Framework=Selenium 4 / Cucumber 7 / TestNG',
    'Branch=main',
    `Build=${run.buildNumber}`,
    'Machine=ci-runner-04',
  ]
  writeFileSync(join(dir, 'environment.properties'), lines.join('\n') + '\n', 'utf-8')
}

function writeExecutorJson(dir, run) {
  const executor = {
    name: 'GitHub Actions',
    type: 'github',
    buildName: 'saucedemo-selenium-sample',
    buildOrder: run.buildNumber,
    buildUrl: `https://github.com/resideo/saucedemo-selenium-sample/actions/runs/${run.buildNumber}`,
    reportUrl: '',
    reportName: 'ResideoNextGen Dashboard',
  }
  writeFileSync(join(dir, 'executor.json'), JSON.stringify(executor, null, 2), 'utf-8')
}

function writeCategoriesJson(dir) {
  const categories = [
    { name: 'Assertion Failure', matchedStatuses: ['failed'], traceRegex: '.*AssertionError.*' },
    { name: 'Element Not Found', matchedStatuses: ['broken'], traceRegex: '.*NoSuchElementException.*' },
    { name: 'Timeout', matchedStatuses: ['broken'], traceRegex: '.*(TimeoutException|WaitTimeoutException).*' },
    { name: 'Stale Element', matchedStatuses: ['broken'], traceRegex: '.*StaleElementReferenceException.*' },
    { name: 'WebDriver Failure', matchedStatuses: ['broken'], traceRegex: '.*(WebDriverException|SessionNotCreatedException).*' },
    { name: 'Not Implemented', matchedStatuses: ['skipped'], traceRegex: '.*PendingException.*' },
  ]
  writeFileSync(join(dir, 'categories.json'), JSON.stringify(categories, null, 2), 'utf-8')
}

for (const run of RUNS) {
  const dir = join(FIXTURES_ROOT, run.key)
  if (existsSync(dir)) rmSync(dir, { recursive: true, force: true })
  mkdirSync(dir, { recursive: true })

  let cursor = Date.parse(run.date)
  for (const scenario of SCENARIOS) {
    const duration = 2000 + Math.floor(Math.random() * 4000)
    const result = buildResult(scenario, run, cursor, duration)
    writeFileSync(join(dir, `${result.uuid}-result.json`), JSON.stringify(result, null, 2), 'utf-8')
    cursor += duration + 500
  }

  writeEnvironmentProperties(dir, run)
  writeExecutorJson(dir, run)
  writeCategoriesJson(dir)

  const counts = SCENARIOS.reduce(
    (acc, s) => {
      const status = run.overrides[s.id]?.status ?? 'passed'
      acc[status] = (acc[status] ?? 0) + 1
      acc.total += 1
      return acc
    },
    { total: 0, passed: 0, failed: 0, broken: 0, skipped: 0 },
  )
  console.log(run.key, JSON.stringify(counts))
}
