ALTER TABLE task_assignments
    ADD COLUMN version bigint NOT NULL DEFAULT 0;

ALTER TABLE task_assignments
    ADD CONSTRAINT task_assignments_skip_check
        CHECK ((status = 'SKIPPED') = (skipped_at IS NOT NULL AND skipped_by_id IS NOT NULL));
