ALTER TABLE task_assignments ADD COLUMN skipped_at timestamp(6);
ALTER TABLE task_assignments ADD COLUMN skipped_by_id uuid;
ALTER TABLE task_assignments ADD CONSTRAINT task_assignments_skipped_by_fk FOREIGN KEY (skipped_by_id) REFERENCES household_members (id);

CREATE INDEX task_assignments_skipped_by_idx ON task_assignments (skipped_by_id);
