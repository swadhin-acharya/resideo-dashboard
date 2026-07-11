# Resideo Automation Dashboard

Real-time test execution dashboard for Cucumber automation frameworks. Streams live execution events, displays pass/fail metrics, and hosts historical reports.

## Architecture

```
┌─────────────────┐       ┌───────────────────┐       ┌────────────┐
│  Local Machine  │  ──►  │  Cloud Backend    │  ──►  │  Database  │
│  (mvn test)     │  ws   │  (Render/Railway)  │       │ (Supabase) │
│  Cucumber Plugin│       │  Spring Boot       │       │ PostgreSQL │
└─────────────────┘       └────────┬──────────┘       └────────────┘
                                   │
                           ┌───────▼────────┐
                           │  Frontend      │
                           │  (Vercel)      │
                           │  React + MUI   │
                           └────────────────┘
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

### Run Frontend (Dev Mode)

```bash
cd resideo-dashboard-ui
npm install
npm run dev
```

Opens at `http://localhost:5173` with API proxy to `http://localhost:8080`.

### Run Cucumber Plugin

```bash
cd sample-denali-framework
mvn test \
  -Dresideo.dashboard.url=http://localhost:8080 \
  -Dresideo.api.token=<your-api-token>
```

---

## Cloud Deployment

### 1. Database — Supabase PostgreSQL

1. Create a [Supabase](https://supabase.com) account and project.
2. Go to **Project Settings → Database → Connection string**.
3. Copy the PostgreSQL connection URI (format: `postgresql://user:password@host:6543/postgres`).

### 2. Backend — Render

1. Fork/clone this repo to GitHub.
2. On [Render](https://render.com), create a **New Web Service**.
3. Connect your GitHub repository.
4. Use these settings:

| Setting | Value |
|---|---|
| **Name** | `resideo-dashboard` |
| **Runtime** | `Docker` |
| **Build Command** | (leave blank — Dockerfile auto-detected) |
| **Start Command** | (leave blank) |
| **Instance Type** | Free |

5. Add these **Environment Variables**:

| Variable | Value |
|---|---|
| `PORT` | `8080` |
| `JDBC_DATABASE_URL` | `jdbc:postgresql://host:6543/postgres?sslmode=require` |
| `JDBC_DATABASE_USERNAME` | `postgres` (from Supabase) |
| `JDBC_DATABASE_PASSWORD` | (from Supabase) |
| `JDBC_DRIVER` | `org.postgresql.Driver` |
| `DB_MODE` | `POSTGRES` |
| `AUTH_ENABLED` | `true` |
| `FRONTEND_URL` | `https://your-frontend.vercel.app` |
| `SPRING_PROFILES_ACTIVE` | `postgres` |
| `LOG_LEVEL` | `INFO` |

6. Deploy. Your backend URL will be `https://resideo-dashboard.onrender.com`.

### 3. Frontend — Vercel

1. Push the repo to GitHub (the `resideo-dashboard-ui/` subdirectory will be detected).
2. On [Vercel](https://vercel.com), import the repository.
3. Set the **Root Directory** to `resideo-dashboard-ui`.
4. Vercel auto-detects the `vercel.json` and Vite config.
5. Add **Environment Variables**:

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://resideo-dashboard.onrender.com/api/v1` |
| `VITE_WS_URL` | `wss://resideo-dashboard.onrender.com/ws` |

6. Deploy. Your frontend URL will be `https://resideo-dashboard.vercel.app`.

### 4. Configure Local Framework to Use Cloud

In `sample-denali-framework/src/test/resources/cucumber.properties`:

```properties
cucumber.plugin=pretty,com.resideo.dashboard.client.cucumber.DashboardCucumberPlugin
resideo.dashboard.url=https://resideo-dashboard.onrender.com
resideo.api.token=rd_<your-cloud-api-token>
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

## Environment Variables Reference

### Backend (`application.yml`)

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | Server port |
| `JDBC_DATABASE_URL` | `jdbc:h2:file:./data/...` | Database JDBC URL (e.g. `jdbc:postgresql://postgres:Resideo%402026%40@db.ocxdpvtifyesevihusrs.supabase.co:5432/postgres?sslmode=require`) |
| `JDBC_DRIVER` | `org.h2.Driver` | JDBC driver class |
| `JDBC_DATABASE_USERNAME` | `sa` | Database user |
| `JDBC_DATABASE_PASSWORD` | *(empty)* | Database password |
| `DB_MODE` | `FILE` | `FILE` for H2, `POSTGRES` for PostgreSQL |
| `AUTH_ENABLED` | `true` | Enable authentication |
| `FRONTEND_URL` | `http://localhost:5173` | CORS allowed origin |
| `CUCUMBER_JSON_PATH` | `target/cucumber.json` | Cucumber JSON report path |
| `REPORT_PATH` | `target/open-reporter/report.html` | HTML report path |
| `STORAGE_TYPE` | `local` | `local` or `supabase` |
| `SUPABASE_URL` | *(empty)* | Supabase project URL (e.g. `https://ocxdpvtifyesevihusrs.supabase.co`) |
| `SUPABASE_SERVICE_KEY` | *(empty)* | Supabase service role key (from Project Settings → API → `service_role` key) |
| `SUPABASE_BUCKET` | `reports` | Supabase storage bucket |
| `LOG_LEVEL` | `INFO` | Logging level |
| `SPRING_PROFILES_ACTIVE` | *(none)* | Set to `postgres` for PostgreSQL |

### Frontend (Vite)

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

# Plugin
mvn test -Dresideo.dashboard.url=http://localhost:8080
```

### Cloud Mode

```bash
# Plugin only (pointing to deployed cloud)
mvn test \
  -Dresideo.dashboard.url=https://resideo-dashboard.onrender.com \
  -Dresideo.api.token=rd_<token> \
  -Dresideo.project.id=<project-uuid>
```

The backend and frontend are already deployed — just run your tests locally and they stream to the cloud dashboard.

---

## API Tokens

Create tokens via the dashboard UI (Settings → API Tokens) or via curl:

```bash
# Login
curl -X POST https://resideo-dashboard.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"...","password":"..."}'

# Create token
curl -X POST https://resideo-dashboard.onrender.com/api/v1/auth/tokens \
  -H "Authorization: Bearer <session-token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"ci-token"}'
```

Response includes `fullToken` — save this value (shown once only).

## First-Time Setup

When you first deploy with a fresh database, the DataSeeder creates:

- Organization: **Resideo**
- Project: **Default**
- Admin user: `admin` / `admin123`

Register additional users via the UI or API.

## License

Internal use — Resideo.
