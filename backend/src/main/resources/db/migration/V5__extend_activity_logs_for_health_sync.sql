ALTER TABLE activity_logs ADD COLUMN source VARCHAR(80);
ALTER TABLE activity_logs ADD COLUMN synced_at TIMESTAMPTZ;
