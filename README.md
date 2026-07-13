# OpenQA Dashboard

Real-time test execution dashboard for Cucumber automation frameworks. Streams live execution results, displays pass/fail metrics, hosts historical reports, and supports distributed execution agents.

## Features

- **Execution Management** — Create, view, and monitor test executions with real-time WebSocket updates
- **Cucumber Report Parsing** — Automatically parse `cucumber.json` into features, scenarios, and steps
- **Distributed Agents** — Agents poll the server, run `mvn test` locally, and push results back
- **Project Scoping** — Multi-project support with project-specific API tokens
- **Role-Based Access** — RBAC with platform admin, project admin, engineer, and viewer roles
- **Report Center** — Generate and download HTML OpenReporter-style reports
- **Analytics Dashboard** — Pass/fail trends, duration charts, platform breakdowns
- **WebSocket Live Updates** — Real-time execution progress streaming to the UI

## Architecture

```
┌─────────────────────────┐       ┌───────────────────────┐
│  Agent (Local Machine)  │       │  Agent (Local Machine)│
│  • Polls for PENDING    │       │  • Polls for PENDING  │
│  • Runs mvn test        │       │  • Runs mvn test      │
│  • Pushes results via   │       │  • Pushes results via │
│    REST API             │       │    REST API           │
└──────────┬──────────────┘       └──────────┬────────────┘
           │  HTTP POST (features,           │
           │  scenarios, steps, logs)        │
           ▼                                 ▼
┌──────────────────────────────────────────────────────┐
│               Cloud Backend (Spring Boot)             │
│  • REST API — /api/v1/executions, /features, /auth   │
│  • WebSocket — /ws (live execution events)           │
│  • Auth — token-based (Bearer oq_...)                │
│  • Reports — OpenReporter HTML generation            │
└────────────────────┬─────────────────────────────────┘
                     │
          ┌──────────▼──────────┐
          │  Database           │
          │  (H2 / PostgreSQL)  │
          └─────────────────────┘

┌──────────────────────────────────────────────────────┐
│              Frontend (React + MUI)                   │
│  • Hosted on Vercel                                   │
│  • Real-time dashboards, reports, analytics           │
│  • Project management & API token creation            │
└──────────────────────────────────────────────────────┘
```

## Quick Start (Local Development)

### Prerequisites

- Java 21+
- Node.js 18+
- Maven 3.9+

### Run Backend

```bash
mvn package -DskipTests
java -jar openqa-dashboard-standalone/target/*.jar --server.port=8080
```

The H2 database file is created at `./data/openqa-dashboard`.  
Default credentials: `admin` / `admin` (created by `DataSampleSeeder`).

### Run Frontend (Dev Mode)

```bash
cd openqa-dashboard-ui
npm install
npm run dev
```

Opens at `http://localhost:5173` with API proxy to `http://localhost:8080`.

### Run Agent (Local)

The agent is embedded in the Spring Boot JAR. Start a separate instance with agent mode enabled:

```bash
java -jar openqa-dashboard-standalone/target/*.jar \
  --server.port=8081 \
  --openqa.agent.enabled=true \
  --openqa.agent.api-url=http://localhost:8080 \
  --openqa.agent.api-token=oq_<your-api-token> \
  --openqa.agent.workspace-path=/path/to/your/cucumber-project
```

The agent polls the server every 5s for `PENDING` executions, runs `mvn test`, and pushes cucumber results.

### Run Cucumber Plugin

The plugin works with any Cucumber-based Java framework. Add the dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.openqa</groupId>
    <artifactId>openqa-dashboard-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Configure via `cucumber.properties`:

```properties
cucumber.plugin=com.openqa.dashboard.client.cucumber.DashboardCucumberPlugin
openqa.dashboard.url=http://localhost:8080
openqa.api.token=oq_<your-api-token>
```

Or via system properties / env vars:

```bash
mvn test \
  -Dopenqa.dashboard.url=http://localhost:8080 \
  -Dopenqa.api.token=oq_<token>

# Or env vars:
OPENQA_DASHBOARD_URL=http://localhost:8080 \
OPENQA_API_TOKEN=oq_<token> \
  mvn test
```

---

## Projects & API Keys

Each execution is scoped to a project. Create projects and API tokens from the dashboard UI (**Settings → Projects** and **Settings → API Tokens**) or via the API.

### Creating a Project

```bash
curl -X POST https://dashboard.openqa.in/api/v1/projects \
  -H "Authorization: Bearer <session-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","slug":"my-project","description":"..."}'
```

Response includes the **project ID** (`id`) — save this for configuration.

### Creating an API Token

```bash
curl -X POST https://dashboard.openqa.in/api/v1/auth/tokens \
  -H "Authorization: Bearer <session-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"ci-token","projectId":"<project-uuid>","expiresInDays":90}'
```

Response includes `fullToken` (e.g., `oq_a1b2c3d4...`) — **save this value** (it is shown only once).

### Configuring a Project for Test Runs

```properties
cucumber.plugin=com.openqa.dashboard.client.cucumber.DashboardCucumberPlugin
openqa.dashboard.url=https://dashboard.openqa.in
openqa.api.token=oq_<your-api-token>
openqa.project.id=<your-project-uuid>
```

---

## Agent Architecture

The agent runs as a separate Spring Boot instance (or multiple instances) and bridges local Maven test execution to the cloud dashboard.

### Lifecycle

1. **User** creates an execution via the UI or API — status is set to `PENDING` with a `mavenCommand` field
2. **Agent** polls `GET /api/v1/executions/agent/pending` (scoped to its API token's user)
3. **Agent** picks up the execution, patches status to `RUNNING`, runs `mvn test` in the configured workspace
4. **Agent** reads `target/cucumber.json`, parses features/scenarios/steps, and POSTs them to the server
5. **Agent** patches execution status to `PASSED` or `FAILED`

### Configuration

| Property | Default | Description |
|---|---|---|
| `openqa.agent.enabled` | `false` | Enable agent mode |
| `openqa.agent.api-url` | — | Server API base URL |
| `openqa.agent.api-token` | — | API token for auth |
| `openqa.agent.workspace-path` | — | Path to the Cucumber project |
| `openqa.agent.poll-interval-ms` | `5000` | Poll interval for pending executions |

---

## Environment Variables Reference

### Backend

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | Server port |
| `JDBC_DATABASE_URL` | `jdbc:h2:file:./data/...` | Database connection URL |
| `JDBC_DRIVER` | `org.h2.Driver` | JDBC driver class |
| `JDBC_DATABASE_USERNAME` | `sa` | Database user |
| `JDBC_DATABASE_PASSWORD` | *(empty)* | Database password |
| `DB_MODE` | `FILE` | `FILE` (H2) or `POSTGRES` |
| `AUTH_ENABLED` | `true` | Enable authentication |
| `FRONTEND_URL` | `http://localhost:5173` | CORS allowed origin |
| `CUCUMBER_JSON_PATH` | `target/cucumber.json` | Cucumber report path |
| `REPORT_PATH` | `target/open-reporter/report.html` | HTML report path |
| `STORAGE_TYPE` | `local` | `local` or `supabase` |
| `SUPABASE_URL` | *(empty)* | Supabase project URL |
| `SUPABASE_SERVICE_KEY` | *(empty)* | Supabase service role key |
| `SUPABASE_BUCKET` | `reports` | Supabase storage bucket |
| `LOG_LEVEL` | `INFO` | Logging level |
| `openqa.agent.enabled` | `false` | Enable agent mode |
| `openqa.seed-sample-data` | `false` | Seed sample data on startup |

### Frontend

| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `/api/v1` | Backend API URL |
| `VITE_WS_URL` | *(none)* | WebSocket URL for live updates |

---

## Cloud Deployment

### 1. Database — Supabase PostgreSQL

1. Create a [Supabase](https://supabase.com) account and project.
2. Go to **Project Settings → Database → Connection string**.
3. Copy the PostgreSQL connection URI.

### 2. Backend — Render

1. Fork/clone this repo to GitHub.
2. On [Render](https://render.com), create a **New Web Service** → connect your repo.
3. Use **Docker** runtime.
4. Add these **Environment Variables**:

| Variable | Value |
|---|---|
| `PORT` | `8080` |
| `JDBC_DATABASE_URL` | `jdbc:postgresql://<host>:6543/postgres?sslmode=require` |
| `JDBC_DATABASE_USERNAME` | (from Supabase) |
| `JDBC_DATABASE_PASSWORD` | (from Supabase) |
| `JDBC_DRIVER` | `org.postgresql.Driver` |
| `DB_MODE` | `POSTGRES` |
| `AUTH_ENABLED` | `true` |
| `FRONTEND_URL` | `https://dashboard.openqa.in` |

### 3. Frontend — Vercel

1. Deploy the `openqa-dashboard-ui` directory to Vercel.
2. Add **Environment Variables**:

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://<your-backend>.onrender.com/api/v1` |
| `VITE_WS_URL` | `wss://<your-backend>.onrender.com/ws` |

3. Add your custom domain `dashboard.openqa.in` in **Vercel → Project → Settings → Domains**.

---

## Integration Guide

Integrate OpenQA Dashboard with any test framework — from zero-code to full custom.

### Method 1: Cucumber Plugin (Real-Time Streaming)

Works with any Cucumber-based Java framework. Streams results live as tests run.

**Add dependency:**
```xml
<dependency>
    <groupId>com.openqa</groupId>
    <artifactId>openqa-dashboard-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Configure (`cucumber.properties`):**
```properties
cucumber.plugin=com.openqa.dashboard.client.cucumber.DashboardCucumberPlugin
openqa.dashboard.url=https://dashboard.openqa.in
openqa.api.token=oq_<your-token>
openqa.project.id=<project-uuid>
```

**Or via env vars:**
```bash
OPENQA_DASHBOARD_URL=https://dashboard.openqa.in \
OPENQA_API_TOKEN=oq_<token> \
OPENQA_PROJECT_ID=<project-uuid> \
  mvn test
```

**How it works:** The plugin hooks into Cucumber lifecycle events (`TestRunStarted`, `TestCaseStarted`, `TestStepFinished`, `TestCaseFinished`, `TestRunFinished`) and pushes features, scenarios, steps, and logs to the server in real-time via REST API. Supports auto-creation of executions or linking to a pre-created one.

---

### Method 2: ExecutionAgent (Zero Code, Any Maven Project)

The agent polls the server, runs `mvn test`, parses `cucumber.json`, and pushes results. No code changes needed in your project.

```bash
java -jar openqa-dashboard-standalone.jar \
  --server.port=8081 \
  --openqa.agent.enabled=true \
  --openqa.agent.api-url=https://dashboard.openqa.in \
  --openqa.agent.api-token=oq_<token> \
  --openqa.agent.workspace-path=/path/to/your/project
```

**Lifecycle:**
1. User creates an execution with a `mavenCommand` via UI/API (status: `PENDING`)
2. Agent polls `GET /api/v1/executions/agent/pending` every 5s
3. Agent picks up the execution, sets status to `RUNNING`, runs `mvn test`
4. Agent reads `target/cucumber.json`, parses features/scenarios/steps, pushes via REST
5. Agent sets final status to `PASSED` or `FAILED`

| Property | Default | Description |
|---|---|---|
| `openqa.agent.enabled` | `false` | Enable agent mode |
| `openqa.agent.api-url` | — | Server API base URL |
| `openqa.agent.api-token` | — | API token for auth |
| `openqa.agent.workspace-path` | — | Path to the Cucumber project |
| `openqa.agent.poll-interval-ms` | `5000` | Poll interval |

---

### Method 3: Direct REST API (Any Language, Any Framework)

Push results from any language — Python, JS, shell scripts, C#, etc.

**Recommended push sequence:**
```bash
# 1. Create execution
curl -X POST https://dashboard.openqa.in/api/v1/executions \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Test Run","source":"CUSTOM"}'

# Save the returned execution ID

# 2. Add a feature
curl -X POST https://dashboard.openqa.in/api/v1/executions/{id}/features \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Login Feature","uri":"features/login.feature","status":"PASSED","durationMs":1500}'

# Save the returned feature ID

# 3. Add a scenario
curl -X POST https://dashboard.openqa.in/api/v1/executions/{id}/scenarios \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"featureId":"<feature-id>","name":"Successful login","status":"PASSED","durationMs":1200}'

# Save the returned scenario ID

# 4. Add steps
curl -X POST https://dashboard.openqa.in/api/v1/executions/{id}/scenarios/{sid}/steps \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"Given","name":"user navigates to login page","status":"PASSED","durationMs":300}'

# 5. Update scenario status (when done)
curl -X PATCH https://dashboard.openqa.in/api/v1/scenarios/{sid}/status \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"PASSED","durationMs":1200}'

# 6. Update execution status (when all done)
curl -X PATCH https://dashboard.openqa.in/api/v1/executions/{id}/status \
  -H "Authorization: Bearer oq_<token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"PASSED"}'
```

---

### Method 4: DashboardReporter (Java, Non-Cucumber)

For JUnit, TestNG, or any Java framework. No Cucumber dependency needed.

**Add dependency:**
```xml
<dependency>
    <groupId>com.openqa</groupId>
    <artifactId>openqa-dashboard-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Usage:**
```java
import com.openqa.dashboard.client.DashboardReporter;

DashboardReporter reporter = new DashboardReporter(
    "https://dashboard.openqa.in",
    "oq_<token>"
);

// Create execution
ExecutionResponse exec = reporter.createExecution(
    new CreateExecutionRequest("My Test Run", "CUSTOM"));

// Add feature
String featureId = reporter.addFeature(exec.id(),
    new FeatureRequest("Login Feature", "features/login.feature"));

// Add scenario
String scenarioId = reporter.addScenario(exec.id(),
    new ScenarioRequest(featureId, "Successful login"));

// Add steps
reporter.addStep(scenarioId,
    new StepRequest("Given", "user navigates to login", "PASSED", 300));
reporter.addStep(scenarioId,
    new StepRequest("When", "user enters credentials", "PASSED", 500));

// Update scenario status
reporter.updateScenarioStatus(scenarioId, "PASSED", 1200, null);

// Update execution status
reporter.updateStatus(exec.id(), "PASSED");
```

**Key methods:**

| Method | API Call |
|---|---|
| `createExecution(req)` | `POST /api/v1/executions` |
| `updateStatus(id, status)` | `PATCH /api/v1/executions/{id}/status` |
| `addLog(id, level, message)` | `POST /api/v1/executions/{id}/logs` |
| `addFeature(execId, req)` | `POST /api/v1/executions/{id}/features` |
| `addScenario(execId, req)` | `POST /api/v1/executions/{id}/scenarios` |
| `addStep(scenarioId, req)` | `POST /api/v1/scenarios/{id}/steps` |
| `completeFeature(execId, featureId)` | `PATCH /api/v1/executions/{id}/features/{fid}/complete` |
| `updateScenarioStatus(id, status, durationMs, failureReason)` | `PATCH /api/v1/scenarios/{id}/status` |

---

### Method 5: WorkspaceWatcher (Server-Side, No Client)

The dashboard watches a shared filesystem for `cucumber.json` changes and auto-ingests them. No client code needed — the server and tests must share the same filesystem.

**Configuration:**
```bash
java -jar openqa-dashboard-standalone.jar \
  --openqa.workspace=/path/to/cucumber-project \
  --openqa.cucumber-json-path=target/cucumber.json
```

The watcher registers a `WatchService` on the workspace directory. When `cucumber.json` changes, it automatically parses features, scenarios, and steps into the database.

---

### Integration Method Comparison

| Method | Real-Time? | Code Changes? | External Dependencies | Language | How It Works |
|---|---|---|---|---|---|
| **1. Cucumber Plugin** | Yes (live) | Add plugin class to `cucumber.properties` | `openqa-dashboard-client` + Cucumber 7.x | Java (Cucumber) | HTTP from test JVM |
| **2. ExecutionAgent** | No (post-run) | None | None (embedded in dashboard JAR) | Any (runs mvn) | Agent runs `mvn test`, parses `cucumber.json`, pushes via REST |
| **3. REST API** | Any cadence | Write HTTP client code | HTTP client | Any language | Direct HTTP calls |
| **4. DashboardReporter** | Yes (programmatic) | Use class in test code | `openqa-dashboard-client` (Jackson only) | Java (any framework) | HTTP via typed Java methods |
| **5. WorkspaceWatcher** | Near-real-time | None (server-side) | Same filesystem | Any | Server watches file, auto-parses |

---

## API Endpoints Reference

| Method | Path | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Login (username + password) |
| `POST` | `/api/v1/auth/register` | Register new user |
| `GET` | `/api/v1/auth/me` | Current user info |
| `GET` | `/api/v1/auth/tokens` | List API tokens |
| `POST` | `/api/v1/auth/tokens` | Create API token |
| `DELETE` | `/api/v1/auth/tokens/{id}` | Revoke token |
| `GET` | `/api/v1/projects` | List projects |
| `POST` | `/api/v1/projects` | Create project |
| `GET` | `/api/v1/projects/{id}` | Get project details |
| `GET` | `/api/v1/executions` | List executions (paginated, filterable) |
| `POST` | `/api/v1/executions` | Create execution (PENDING) |
| `POST` | `/api/v1/executions/trigger` | Create + trigger agent execution |
| `GET` | `/api/v1/executions/{id}` | Execution details (with features + scenarios) |
| `PATCH` | `/api/v1/executions/{id}/status` | Update execution status |
| `PATCH` | `/api/v1/executions/{id}/name` | Update execution name |
| `DELETE` | `/api/v1/executions/{id}` | Delete execution and all child records |
| `POST` | `/api/v1/executions/{id}/cancel` | Cancel running execution (kills agent process) |
| `GET` | `/api/v1/executions/summary` | Aggregate summary (total, passed, failed, pass rate) |
| `GET` | `/api/v1/executions/running` | List currently running executions |
| `GET` | `/api/v1/executions/feature-files` | List `.feature` files in workspace |
| `GET` | `/api/v1/executions/agent/pending` | List pending executions (agent) |
| `POST` | `/api/v1/executions/{id}/features` | Add feature to execution |
| `PATCH` | `/api/v1/executions/{id}/features/{fid}/complete` | Mark feature as complete |
| `POST` | `/api/v1/executions/{id}/scenarios` | Add scenario to execution |
| `POST` | `/api/v1/executions/{id}/scenarios/{sid}/steps` | Add step to scenario |
| `POST` | `/api/v1/executions/{id}/logs` | Append execution log |
| `GET` | `/api/v1/executions/{id}/logs` | Get execution logs |
| `GET` | `/api/v1/executions/{id}/report` | Get HTML report (inline) |
| `GET` | `/api/v1/executions/{id}/report/download` | Download HTML report |
| `POST` | `/api/v1/executions/{id}/report/email` | Email report |
| `GET` | `/api/v1/reports` | List all reports |
| `POST` | `/api/v1/scenarios/{sid}/steps` | Add step to scenario |
| `PATCH` | `/api/v1/scenarios/{sid}/status` | Update scenario status |

---

## Switching Between Local and Cloud

### Local Mode

```bash
# Backend
java -jar openqa-dashboard-standalone/target/*.jar

# Frontend
cd openqa-dashboard-ui && npm run dev

# Agent
java -jar openqa-dashboard-standalone/target/*.jar \
  --server.port=8081 --openqa.agent.enabled=true \
  --openqa.agent.api-url=http://localhost:8080 \
  --openqa.agent.api-token=oq_<token> \
  --openqa.agent.workspace-path=./sample-framework

# Plugin (if not using agent)
mvn test -Dopenqa.dashboard.url=http://localhost:8080
```

### Cloud Mode

```bash
mvn test \
  -Dopenqa.dashboard.url=https://dashboard.openqa.in \
  -Dopenqa.api.token=oq_<token> \
  -Dopenqa.project.id=<project-uuid>
```

---

## License

OpenQA — Internal use.
