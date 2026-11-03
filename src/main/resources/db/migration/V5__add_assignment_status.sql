ALTER TABLE task_assignments
    ADD COLUMN status varchar(255) NOT NULL DEFAULT 'OPEN';

UPDATE task_assignments SET status = 'COMPLETED'
WHERE id IN (SELECT task_assignment_id FROM task_completions);

UPDATE task_assignments SET status = 'SKIPPED'
WHERE skipped_at IS NOT NULL;

ALTER TABLE task_assignments
    ADD CONSTRAINT task_assignments_status_check CHECK (status IN ('OPEN', 'COMPLETED', 'SKIPPED'));
