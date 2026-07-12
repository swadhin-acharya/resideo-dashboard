# Resideo Automation Dashboard

Real-time test execution dashboard for Cucumber automation frameworks. Streams live execution results, displays pass/fail metrics, hosts historical reports, and supports distributed execution agents.

## Features

- **Execution Management** — Create, view, and monitor test executions with real-time WebSocket updates
- **Cucumber Report Parsing** — Automatically parse `cucumber.json` into features, scenarios, and steps
- **Distributed Agents** — Agents poll the server, run `mvn test` locally, and push results back
- **Project Scoping** — Multi-project support with project-specific API tokens
- **Role-Based Access** — RBAC with platform admin, project admin, engineer, and viewer roles
- **Report Center** — Generate and download HTML OpenReporter-style reports
- **Analytics Dashboard** — Pass/fail trends, duration charts, platform breakdowns
- **Supabase Integration** — Optional cloud storage for reports and artifacts
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
│                Cloud Backend (Spring Boot)            │
│  • REST API — /api/v1/executions, /features, /auth   │
│  • WebSocket — /ws (live execution events)           │
│  • Auth — token-based (Bearer rd_...)                │
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
java -jar resideo-dashboard-standalone/target/*.jar --server.port=8080
```

The H2 database file is created at `./data/resideo-dashboard`.  
Default credentials: `admin` / `admin` (created by `DataSampleSeeder`).

### Run Frontend (Dev Mode)

```bash
cd resideo-dashboard-ui
npm install
npm run dev
```

Opens at `http://localhost:5173` with API proxy to `http://localhost:8080`.

### Run Agent (Local)

The agent is embedded in the Spring Boot JAR. Start a separate instance with agent mode enabled:

```bash
java -jar resideo-dashboard-standalone/target/*.jar \
  --server.port=8081 \
  --resideo.agent.enabled=true \
  --resideo.agent.api-url=http://localhost:8080 \
  --resideo.agent.api-token=rd_<your-api-token> \
  --resideo.agent.workspace-path=/path/to/your/cucumber-project
```

The agent polls the server every 5s for `PENDING` executions, runs `mvn test`, and pushes cucumber results.

### Run Cucumber Plugin

```bash
cd sample-denali-framework
mvn test \
  -Dresideo.dashboard.url=http://localhost:8080 \
  -Dresideo.api.token=<your-api-token>
```

---

## Projects & API Keys

Each execution is scoped to a project. Create projects and API tokens from the dashboard UI (**Settings → Projects** and **Settings → API Tokens**) or via the API.

### Creating a Project

```bash
curl -X POST https://resideo-dashboard.onrender.com/api/v1/projects \
  -H "Authorization: Bearer <session-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","slug":"my-project","description":"..."}'
```

Response includes the **project ID** (`id`) — save this for configuration.

### Creating an API Token

```bash
curl -X POST https://resideo-dashboard.onrender.com/api/v1/auth/tokens \
  -H "Authorization: Bearer <session-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"ci-token","projectId":"<project-uuid>","expiresInDays":90}'
```

Response includes `fullToken` (e.g., `rd_a1b2c3d4...`) — **save this value** (it is shown only once).

### Configuring a Project for Test Runs

In `cucumber.properties`:

```properties
cucumber.plugin=pretty,com.resideo.dashboard.client.cucumber.DashboardCucumberPlugin
resideo.dashboard.url=https://resideo-dashboard.onrender.com
resideo.api.token=rd_<your-api-token>
resideo.project.id=<your-project-uuid>
```

Or via system properties:

```bash
mvn test \
  -Dresideo.dashboard.url=https://resideo-dashboard.onrender.com \
  -Dresideo.api.token=rd_<token> \
  -Dresideo.project.id=<project-uuid>
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
| `resideo.agent.enabled` | `false` | Enable agent mode |
| `resideo.agent.api-url` | — | Server API base URL |
| `resideo.agent.api-token` | — | API token for auth |
| `resideo.agent.workspace-path` | — | Path to the Cucumber project |
| `resideo.agent.poll-interval-ms` | `5000` | Poll interval for pending executions |

---

## Cloud Deployment

### 1. Database — Supabase PostgreSQL

1. Create a [Supabase](https://supabase.com) account and project.
2. Go to **Project Settings → Database → Connection string**.
3. Copy the PostgreSQL connection URI (`postgresql://user:password@host:6543/postgres`).

### 2. Backend — Render

1. Fork/clone this repo to GitHub.
2. On [Render](https://render.com), create a **New Web Service** → connect your repo.
3. Settings:

| Setting | Value |
|---|---|
| **Name** | `resideo-dashboard` |
| **Runtime** | `Docker` |
| **Instance Type** | Free |

4. Environment Variables:

| Variable | Value |
|---|---|
| `PORT` | `8080` |
| `JDBC_DATABASE_URL` | `jdbc:postgresql://host:6543/postgres?sslmode=require` |
| `JDBC_DATABASE_USERNAME` | (from Supabase) |
| `JDBC_DATABASE_PASSWORD` | (from Supabase) |
| `JDBC_DRIVER` | `org.postgresql.Driver` |
| `DB_MODE` | `POSTGRES` |
| `AUTH_ENABLED` | `true` |
| `FRONTEND_URL` | `https://resideo-dashboard-ui.vercel.app` |

### 3. Frontend — Vercel

The frontend is already deployed at **https://resideo-dashboard-ui.vercel.app**.

To redeploy after changes:

```bash
cd resideo-dashboard-ui
vercel --prod
```

The Vercel project uses environment variables from `vercel.json`:

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://resideo-dashboard.onrender.com/api/v1` |
| `VITE_WS_URL` | `wss://resideo-dashboard.onrender.com/ws` |

### 4. API Endpoints Reference

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login (username + password) |
| `POST` | `/api/v1/auth/register` | Register new user |
| `GET` | `/api/v1/auth/me` | Current user info |
| `GET` | `/api/v1/auth/tokens` | List API tokens |
| `POST` | `/api/v1/auth/tokens` | Create API token |
| `DELETE` | `/api/v1/auth/tokens/{id}` | Revoke token |
| `GET` | `/api/v1/projects` | List projects |
| `POST` | `/api/v1/projects` | Create project |
| `GET` | `/api/v1/projects/{id}` | Get project details |
| `GET` | `/api/v1/executions` | List executions |
| `POST` | `/api/v1/executions` | Create execution |
| `GET` | `/api/v1/executions/{id}` | Execution details |
| `GET` | `/api/v1/executions/agent/pending` | List pending executions (agent) |
| `POST` | `/api/v1/executions/{id}/features` | Add feature to execution |
| `POST` | `/api/v1/executions/{id}/scenarios` | Add scenario to execution |
| `POST` | `/api/v1/executions/{id}/scenarios/{sid}/steps` | Add step to scenario |
| `PATCH` | `/api/v1/executions/{id}/status` | Update execution status |
| `POST` | `/api/v1/executions/{id}/logs` | Append execution log |

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
| `resideo.agent.enabled` | `false` | Enable agent mode |
| `resideo.seed-sample-data` | `false` | Seed sample data on startup |

### Frontend

| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `/api/v1` | Backend API URL |
| `VITE_WS_URL` | *(none)* | WebSocket URL for live updates |

---

## Switching Between Local and Cloud

### Local Mode

```bash
# Backend
java -jar resideo-dashboard-standalone/target/*.jar

# Frontend
cd resideo-dashboard-ui && npm run dev

# Agent
java -jar resideo-dashboard-standalone/target/*.jar \
  --server.port=8081 --resideo.agent.enabled=true \
  --resideo.agent.api-url=http://localhost:8080 \
  --resideo.agent.api-token=rd_<token> \
  --resideo.agent.workspace-path=./sample-framework

# Plugin (if not using agent)
mvn test -Dresideo.dashboard.url=http://localhost:8080
```

### Cloud Mode

```bash
# Plugin only (results stream to cloud dashboard)
mvn test \
  -Dresideo.dashboard.url=https://resideo-dashboard.onrender.com \
  -Dresideo.api.token=rd_<token> \
  -Dresideo.project.id=<project-uuid>
```

---

## First-Time Setup

When deployed with a fresh database, the `DataSeeder` creates:

- Organization: **Resideo**
- Project: **Default Project**
- Admin user: `admin` / `admin` (if `resideo.seed-sample-data=true`)
- Default users: `admin` / `admin`, `swadhin.acharya` / `swadhin`

Register additional users via the UI or API.

## License

Internal use — Resideo.
