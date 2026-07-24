CREATE TABLE IF NOT EXISTS executions (
    id TEXT PRIMARY KEY,
    name TEXT,
    framework TEXT DEFAULT 'CUCUMBER',
    branch TEXT,
    machine TEXT,
    platform TEXT,
    environment TEXT,
    build_number TEXT,
    status TEXT DEFAULT 'PENDING',
    execution_type TEXT DEFAULT 'REGRESSION',
    start_time TEXT,
    end_time TEXT,
    duration_ms BIGINT DEFAULT 0,
    pass_count INTEGER DEFAULT 0,
    fail_count INTEGER DEFAULT 0,
    skip_count INTEGER DEFAULT 0,
    total_count INTEGER DEFAULT 0,
    report_path TEXT,
    log_path TEXT,
    trigger_source TEXT DEFAULT 'MANUAL',
    maven_command TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS features (
    id TEXT PRIMARY KEY,
    execution_id TEXT NOT NULL,
    name TEXT,
    uri TEXT,
    status TEXT DEFAULT 'PENDING',
    duration_ms BIGINT DEFAULT 0,
    pass_count INTEGER DEFAULT 0,
    fail_count INTEGER DEFAULT 0,
    skip_count INTEGER DEFAULT 0,
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scenarios (
    id TEXT PRIMARY KEY,
    execution_id TEXT NOT NULL,
    feature_id TEXT,
    name TEXT,
    tags TEXT,
    status TEXT DEFAULT 'UNKNOWN',
    duration_ms BIGINT DEFAULT 0,
    failure_reason TEXT,
    device_name TEXT,
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES features(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id TEXT NOT NULL,
    keyword TEXT DEFAULT '',
    name TEXT,
    status TEXT DEFAULT 'UNKNOWN',
    duration_ms BIGINT DEFAULT 0,
    log_text TEXT,
    FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    execution_id TEXT NOT NULL,
    level TEXT DEFAULT 'INFO',
    message TEXT,
    timestamp TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reports (
    id TEXT PRIMARY KEY,
    execution_id TEXT NOT NULL,
    name TEXT,
    file_path TEXT,
    file_size BIGINT DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS devices (
    id TEXT PRIMARY KEY,
    device_id TEXT UNIQUE,
    brand TEXT,
    model TEXT,
    os_version TEXT,
    platform TEXT DEFAULT 'ANDROID',
    status TEXT DEFAULT 'ONLINE',
    last_seen TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT UNIQUE NOT NULL,
    value TEXT
);

CREATE INDEX IF NOT EXISTS idx_features_execution ON features(execution_id);
CREATE INDEX IF NOT EXISTS idx_scenarios_execution ON scenarios(execution_id);
CREATE INDEX IF NOT EXISTS idx_scenarios_feature ON scenarios(feature_id);
CREATE INDEX IF NOT EXISTS idx_steps_scenario ON steps(scenario_id);
CREATE INDEX IF NOT EXISTS idx_logs_execution ON logs(execution_id);
CREATE INDEX IF NOT EXISTS idx_reports_execution ON reports(execution_id);
CREATE INDEX IF NOT EXISTS idx_executions_status ON executions(status);
CREATE INDEX IF NOT EXISTS idx_executions_created ON executions(created_at);
