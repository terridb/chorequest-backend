\set ON_ERROR_STOP on

\if :{?sub}
\else
    \echo 'Pass the Auth0 sub of the account to use: -v sub=auth0|...'
    \quit
\endif

SET TIME ZONE 'Europe/Amsterdam';
\set ts '(now() AT TIME ZONE ''UTC'')'

SELECT count(*) = 1 AS found FROM users WHERE auth0_sub = :'sub' \gset
\if :found
\else
    \echo 'No user with that sub yet. Log in once on the local frontend first.'
    \quit
\endif

BEGIN;

DELETE FROM task_completions
WHERE task_assignment_id IN (
    SELECT ta.id FROM task_assignments ta
    JOIN tasks t ON t.id = ta.task_id
    WHERE t.household_id = 'dede0000-0000-0000-0000-000000000001'
);
DELETE FROM task_assignments
WHERE task_id IN (SELECT id FROM tasks WHERE household_id = 'dede0000-0000-0000-0000-000000000001');
DELETE FROM tasks WHERE household_id = 'dede0000-0000-0000-0000-000000000001';
DELETE FROM goals WHERE household_id = 'dede0000-0000-0000-0000-000000000001';
DELETE FROM household_invites WHERE household_id = 'dede0000-0000-0000-0000-000000000001';
DELETE FROM household_members WHERE household_id = 'dede0000-0000-0000-0000-000000000001';
DELETE FROM households WHERE id = 'dede0000-0000-0000-0000-000000000001';
DELETE FROM users WHERE auth0_sub LIKE 'demo|%';

UPDATE users
SET first_name = 'Chris', last_name = 'Visser', avatar_url = NULL, updated_at = :ts
WHERE auth0_sub = :'sub';

INSERT INTO users (id, created_at, updated_at, first_name, last_name, avatar_url, auth0_sub)
VALUES
    ('dede0000-0000-0000-0000-00000000b002', :ts, :ts, 'Noor', 'Bakker', NULL, 'demo|noor'),
    ('dede0000-0000-0000-0000-00000000b003', :ts, :ts, 'Daan', 'de Vries', NULL, 'demo|daan'),
    ('dede0000-0000-0000-0000-00000000b004', :ts, :ts, 'Lisa', 'Jansen', NULL, 'demo|lisa');

INSERT INTO households (id, created_at, updated_at, name, type, photo_url, city, zipcode, time_zone)
VALUES ('dede0000-0000-0000-0000-000000000001', :ts - INTERVAL '60 days', :ts,
        'Maple Street House', 'SHARED', NULL, 'Utrecht', '3511AB', 'Europe/Amsterdam');

INSERT INTO household_members (id, created_at, updated_at, household_id, user_id, points, role)
VALUES
    ('dede0000-0000-0000-0000-00000000a001', :ts - INTERVAL '60 days', :ts, 'dede0000-0000-0000-0000-000000000001',
     (SELECT id FROM users WHERE auth0_sub = :'sub'), 0, 'ADMIN'),
    ('dede0000-0000-0000-0000-00000000a002', :ts - INTERVAL '59 days', :ts, 'dede0000-0000-0000-0000-000000000001',
     'dede0000-0000-0000-0000-00000000b002', 0, 'MEMBER'),
    ('dede0000-0000-0000-0000-00000000a003', :ts - INTERVAL '58 days', :ts, 'dede0000-0000-0000-0000-000000000001',
     'dede0000-0000-0000-0000-00000000b003', 0, 'MEMBER'),
    ('dede0000-0000-0000-0000-00000000a004', :ts - INTERVAL '57 days', :ts, 'dede0000-0000-0000-0000-000000000001',
     'dede0000-0000-0000-0000-00000000b004', 0, 'MEMBER');

INSERT INTO tasks (id, created_at, updated_at, title, description, effort, frequency, starting_date, always_same_day,
                   default_points, strategy, responsible_member_id, household_id)
VALUES
    ('dede0000-0000-0000-0000-00000000c001', :ts, :ts, 'Do the dishes', 'Wash, dry and put everything back in the cupboards',
     'SMALL', 'DAILY', CURRENT_DATE - 30, false, 10, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c002', :ts, :ts, 'Take out the trash', 'Trash, paper and plastic go out to the curb',
     'SMALL', 'EVERY_OTHER_DAY', CURRENT_DATE - 30, false, 10, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c003', :ts, :ts, 'Vacuum the living room', 'Living room, hallway and the stairs',
     'MEDIUM', 'WEEKLY', CURRENT_DATE - 30, false, 15, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c004', :ts, :ts, 'Clean the bathroom', 'Shower, toilet, sink and mirror',
     'BIG', 'WEEKLY', CURRENT_DATE - 30, false, 30, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c005', :ts, :ts, 'Water the plants', 'Including the ones on the balcony',
     'NONE', 'EVERY_THREE_DAYS', CURRENT_DATE - 30, false, 5, 'SOLO', 'dede0000-0000-0000-0000-00000000a002',
     'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c006', :ts, :ts, 'Weekly groceries', 'Check the shared list before you go',
     'MEDIUM', 'WEEKLY', CURRENT_DATE - 30, false, 15, 'SOLO', 'dede0000-0000-0000-0000-00000000a001',
     'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c007', :ts, :ts, 'Change the bed sheets', 'Spare sets are in the hallway closet',
     'SMALL', 'EVERY_OTHER_WEEK', CURRENT_DATE - 30, false, 10, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c008', :ts, :ts, 'Defrost the freezer', 'Put a towel down first',
     'MEDIUM', 'EVERY_THREE_MONTHS', CURRENT_DATE - 160, false, 15, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c009', :ts, :ts, 'Restock toilet paper', NULL,
     'NONE', 'AS_NEEDED', CURRENT_DATE - 30, false, 5, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c010', :ts, :ts, 'Descale the kettle', 'Vinegar and water, then rinse twice',
     'SMALL', 'AS_NEEDED', CURRENT_DATE - 30, false, 10, 'ROTATING', NULL, 'dede0000-0000-0000-0000-000000000001'),
    ('dede0000-0000-0000-0000-00000000c011', :ts, :ts, 'Fix the leaky tap', 'Washers are in the toolbox under the sink',
     'BIG', 'ONCE', CURRENT_DATE + 4, false, 30, 'SOLO', 'dede0000-0000-0000-0000-00000000a003',
     'dede0000-0000-0000-0000-000000000001');

INSERT INTO goals (id, created_at, updated_at, household_id, goal_points, title, description, completion_date,
                   current_points, status)
VALUES ('dede0000-0000-0000-0000-00000000d001', :ts - INTERVAL '9 days', :ts, 'dede0000-0000-0000-0000-000000000001',
        300, 'Pizza night', 'Homemade pizza and a movie for the whole house', NULL, 0, 'ACTIVE');

WITH history(task_suffix, member_suffix, days_ago) AS (
    VALUES
        ('c001', 'a002', 1), ('c001', 'a003', 2), ('c001', 'a004', 3), ('c001', 'a001', 4), ('c001', 'a002', 5),
        ('c001', 'a003', 6), ('c001', 'a004', 7), ('c001', 'a001', 8), ('c001', 'a002', 9),
        ('c002', 'a001', 3), ('c002', 'a004', 5), ('c002', 'a003', 7), ('c002', 'a002', 9),
        ('c003', 'a004', 5), ('c003', 'a003', 12),
        ('c004', 'a001', 7), ('c004', 'a002', 14),
        ('c005', 'a002', 2), ('c005', 'a002', 5), ('c005', 'a002', 8), ('c005', 'a002', 11),
        ('c006', 'a001', 4), ('c006', 'a001', 11),
        ('c007', 'a003', 8),
        ('c008', 'a004', 70),
        ('c009', 'a004', 6)
),
completed AS (
    INSERT INTO task_assignments (id, created_at, updated_at, household_member_id, task_id, due_date)
    SELECT gen_random_uuid(), :ts - make_interval(days => days_ago + 1), :ts,
           ('dede0000-0000-0000-0000-00000000' || member_suffix)::uuid,
           ('dede0000-0000-0000-0000-00000000' || task_suffix)::uuid,
           CURRENT_DATE - days_ago
    FROM history
    RETURNING id, task_id, household_member_id, due_date
)
INSERT INTO task_completions (id, created_at, updated_at, completed_at, rewarded_points, task_assignment_id,
                              completed_by_id, goal_id)
SELECT gen_random_uuid(), :ts, :ts, c.due_date + TIME '18:30', t.default_points, c.id, c.household_member_id,
       CASE WHEN c.due_date >= CURRENT_DATE - 9 THEN 'dede0000-0000-0000-0000-00000000d001'::uuid END
FROM completed c
JOIN tasks t ON t.id = c.task_id;

INSERT INTO task_assignments (id, created_at, updated_at, household_member_id, task_id, due_date)
VALUES
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a001', 'dede0000-0000-0000-0000-00000000c001', CURRENT_DATE),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a003', 'dede0000-0000-0000-0000-00000000c002', CURRENT_DATE - 1),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a002', 'dede0000-0000-0000-0000-00000000c003', CURRENT_DATE + 2),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a004', 'dede0000-0000-0000-0000-00000000c004', CURRENT_DATE),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a002', 'dede0000-0000-0000-0000-00000000c005', CURRENT_DATE + 1),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a001', 'dede0000-0000-0000-0000-00000000c006', CURRENT_DATE + 3),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a004', 'dede0000-0000-0000-0000-00000000c007', CURRENT_DATE + 6),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a001', 'dede0000-0000-0000-0000-00000000c008', CURRENT_DATE + 20),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a001', 'dede0000-0000-0000-0000-00000000c009', NULL),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a002', 'dede0000-0000-0000-0000-00000000c010', NULL),
    (gen_random_uuid(), :ts, :ts, 'dede0000-0000-0000-0000-00000000a003', 'dede0000-0000-0000-0000-00000000c011', CURRENT_DATE + 4);

UPDATE household_members m
SET points = coalesce((SELECT sum(rewarded_points) FROM task_completions WHERE completed_by_id = m.id), 0)
WHERE m.household_id = 'dede0000-0000-0000-0000-000000000001';

UPDATE goals g
SET current_points = coalesce((SELECT sum(rewarded_points) FROM task_completions WHERE goal_id = g.id), 0)
WHERE g.id = 'dede0000-0000-0000-0000-00000000d001';

COMMIT;

\echo 'Demo household ready: Maple Street House'
