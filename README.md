# ResideoNextGen Dashboard

A standalone, static automation analytics dashboard powered by Allure result
data. Built with React, TypeScript, Vite, Material UI, and Recharts —
deployable to GitHub Pages with no backend.

Allure is the single source of truth. This project never invents or
independently recalculates totals, pass rates, or health scores — it reads
Allure's structured result files, normalizes them into one canonical model,
and the UI only visualizes that model.

## What is ResideoNextGen Dashboard?

Think of the pipeline as:

```
Automation Framework → Allure Adapter → allure-results/
    → ResideoNextGen Processor → Static Dashboard JSON
    → ResideoNextGen React Dashboard → GitHub Actions → GitHub Pages
```

The user only ever sees "ResideoNextGen Dashboard" — Allure is the
underlying engine, never exposed, never iframed, never re-skinned.

## Status

All five milestones from the project brief are implemented:

1. **UI (mock data)** — Overview page, dark/light theme, app shell.
2. **Allure processor** — `processor/` reads `allure-results/`, produces
   canonical JSON, unit-tested against a controlled fixture.
3. **Real data wired in** — `public/data/*.json` currently holds processor
   output from the [Sauce Demo Selenium sample](../saucedemo-selenium-sample),
   not mock data. See "Sample data" below.
4. **History preservation** — the processor merges each execution into
   persisted history, trimmed to a configurable `historyLimit`.
5. **GitHub Actions + Pages** — `.github/workflows/deploy.yml` and a
   reusable `action.yml` (see "GitHub Actions integration").

The other nav sections (Executions, Features, Tests, Failure Analysis,
Retries, Trends, History, Reports, Environment) are intentionally disabled
("Coming soon") — only Overview is built out today.

A sixth piece, **`java-reporter/`**, is a separate, optional Java/Maven
integration: any TestNG or Cucumber project that already has an Allure
adapter can add it as a dependency to process results and view this same
dashboard locally with no Node/npm involved — see
[`java-reporter/README.md`](java-reporter/README.md).

## Architecture

- **Frontend**: React + TypeScript + Vite + Material UI + Recharts +
  React Router (`HashRouter` — see "GitHub Pages routing" below).
- **Processor**: plain Node/TypeScript under `processor/`, compiled with
  `tsc` and run directly with `node` (no bundler, no extra runtime
  dependencies beyond what's already in this repo).
- **Canonical model**: `src/types/models.ts` (frontend) and
  `processor/src/models.ts` (processor) describe the same shapes — see the
  note at the top of the latter for why they're two files, not one.
- **No backend**: everything the browser needs is a static JSON file under
  `public/data/`.

## Local development

```bash
npm install
npm run dev
```

Open the printed local URL (typically `http://localhost:5173`).

## Allure integration

### Running the processor

```bash
npm run process-results -- \
  --allure-results /path/to/allure-results \
  --out public/data \
  --execution-id 226 \
  --history-limit 50
```

- `--allure-results` (required): a directory containing `*-result.json`,
  `environment.properties`, `executor.json`, `categories.json` — the
  standard output of any Allure adapter (TestNG, JUnit, Cucumber, etc).
- `--execution-id`: defaults to the Allure executor's `buildOrder`, then a
  timestamp, if omitted.
- `--out`: defaults to `public/data`.
- `--history-limit`: defaults to 50 (see "Historical data" below).

Re-running the processor for an `--execution-id` that's already in history
**replaces** that entry rather than duplicating it or double-counting its
failures — reprocessing the same CI run twice is always safe.

### What the processor reads

| File | Used for |
|---|---|
| `*-result.json` | test name, status, timing, steps, labels (feature/severity/tag), historyId |
| `environment.properties` | OS, Java, platform, browser, framework, branch, build, machine |
| `executor.json` | executor name/type, build name/order/URL |
| `categories.json` | custom failure categorization rules (regex against status + trace) — falls back to grouping by exception class name if absent |

### What it produces

`public/data/summary.json`, `executions.json`, `trends.json`,
`features.json`, `tests.json`, `failures.json`, `categories.json`,
`environment.json`, plus an internal `.failures-contributions.json` (not
fetched by the dashboard — bookkeeping so reprocessing stays idempotent).

### Testing the processor

```bash
npm run test:processor
```

Runs against the controlled fixtures in `processor/test/fixtures/run-1`,
`run-2`, `run-3` (three real, hand-designed Allure result sets with a
deliberate passed/failed/broken/skipped mix — see the sample project below).
Assertions check exact totals (Milestone 2's acceptance-test style) and the
full history-merge lifecycle (Milestone 4): accumulation across three
executions, `historyLimit` trimming, and idempotent reprocessing.

## Sample data

`public/data/*.json` is currently populated from
[`saucedemo-selenium-sample`](../saucedemo-selenium-sample), a small
Selenium + Cucumber + TestNG project that exists purely to generate real
Allure output to validate this processor against — not a real Resideo
product suite. See that project's README for what it tests and why its
results are a deliberate mix rather than all green.

The original polished mock data from Milestone 1 (1,248 tests, 93.27% pass
rate) is preserved in `mock-data-reference/` for comparison — it's outside
`public/`, so it's never bundled into a production build.

## Configuration

`public/config.json`:

```json
{
  "projectName": "Sauce Demo Sample Automation",
  "dashboardTitle": "ResideoNextGen Dashboard",
  "dashboardSubtitle": "Automation Intelligence & Reporting",
  "historyLimit": 50,
  "reportRetention": 20
}
```

Only `projectName` needs to change for a different automation project.
Everything else — components, processor, routing — works unmodified.

## GitHub Actions integration

Two ways to use this project in CI, both under `.github/workflows/` and
`action.yml`:

**Direct** (`deploy.yml`, this repo's own demo): checkout, install, restore
previous history from the `gh-pages` branch, run processor tests, process a
sample `allure-results` fixture, build, deploy. Runnable as-is with zero
external setup.

**Reusable action** (`action.yml`, for other Resideo automation projects):

```yaml
- uses: resideo/resideo-nextgen-dashboard@main
  with:
    project-name: 'My Project Automation'
    allure-results-path: allure-results
    history-path: previous-gh-pages/data   # optional, omit on first run
    history-limit: 50
```

The action checks out the dashboard source itself, so a consuming project's
repo only needs its own `allure-results/` — it never needs a copy of this
codebase. See `action.yml` for the full input list (including optional
OpenReporter linking and a custom `base-path`).

## GitHub Pages setup

1. In the repo's Settings → Pages, set the source to "GitHub Actions".
2. Push to `main` (or run the workflow manually) — `deploy.yml` builds and
   publishes `dist/`.
3. The dashboard uses `HashRouter`, not browser-history routing (see below),
   so no extra Pages configuration (like a `404.html` redirect trick) is
   needed for deep-link refreshes to work.

### GitHub Pages routing

Vite's `base` is read from `VITE_BASE_PATH` (see `vite.config.ts`) so the
build knows it's served from `https://<org>.github.io/<repo>/`, not `/`.
Routing itself uses React Router's `HashRouter` rather than browser-history
routing — GitHub Pages has no server-side rewrite rules, so a URL like
`/<repo>/executions/225` would 404 on refresh under browser-history routing.
Hash routing (`/#/executions/225`) keeps all navigation client-side and
survives a hard refresh under any subpath.

## Adding to a new automation project

1. Point your test framework's Allure adapter at `allure-results/` as usual
   — no changes needed there.
2. In your project's CI, add the reusable action step above with your
   project's `project-name` and `allure-results-path`.
3. Add a `gh-pages`-restore step (see `deploy.yml` for the exact pattern) if
   you want trends/history to span more than one run.
4. Enable GitHub Pages (source: GitHub Actions) in that repo's settings.

No fork of this repository, no copy-pasted component code, no shared
database — each project gets its own independent dashboard from its own
Allure data.

## Historical data

- `historyLimit` (default 50, configurable via `public/config.json` and the
  processor's `--history-limit` flag) caps how many executions' worth of
  `executions.json` / `trends.json` are retained.
- Top-failure occurrence counts (`failures.json`) are tracked separately
  from that limit and are not trimmed by it — "keep lightweight historical
  analytics longer than heavy per-run data," per the brief.
- Re-running the processor for an execution ID already in history replaces
  it in place; a genuinely new execution ID always accumulates.
- There is no `public/data/history/<id>.json` per-execution snapshot
  directory (as sketched in the original brief) — `executions.json` +
  `trends.json` already carry everything the current UI needs, and adding
  a second redundant storage format would be over-engineering for what's
  actually used today. Revisit if/when the Execution Details or Retries
  pages need raw per-execution payloads this flat model doesn't capture.

## Troubleshooting

**"Unable to load dashboard data" on the Overview page** — `public/data/*.json`
is missing or malformed. Run `npm run process-results` (or restore
`mock-data-reference/*.json` into `public/data/` for a quick sanity check)
and reload.

**Processor throws "allure-results directory not found"** — check the
`--allure-results` path is correct and that your test run actually produced
`*-result.json` files (an empty directory, or one containing only
`environment.properties`, will fail this check).

**A category always shows 0 tests** — categories only appear if at least one
result actually matched their rule; this is deliberate (brief section 21:
"Do NOT hardcode these as actual results"). Check `categories.json`'s
`traceRegex`/`messageRegex` against your actual stack traces.

**GitHub Pages shows a blank page after deploy** — almost always a `base`
mismatch. Confirm `VITE_BASE_PATH` in the workflow matches your repo name
exactly (`/my-repo/`, with both leading and trailing slashes).

**Numbers differ between the KPI cards and a chart** — this should be
structurally impossible (both read the same `summary.json`/`trends.json`),
so if it happens, it's a bug in a component reading the wrong field, not a
calculation discrepancy — every calculation happens exactly once, in the
processor.

## Project layout

```
src/                   React dashboard (see src/pages, src/components)
processor/
  src/                 reader.ts, normalize.ts, history.ts, writer.ts, process.ts, cli.ts
  test/                normalize.test.ts, history.test.ts, fixtures/run-{1,2,3}/
public/
  config.json          per-project configuration
  data/                canonical JSON the dashboard fetches at runtime
mock-data-reference/    Milestone 1's original mock data (not bundled into builds)
action.yml              reusable composite GitHub Action
.github/workflows/      this repo's own CI/CD
```
