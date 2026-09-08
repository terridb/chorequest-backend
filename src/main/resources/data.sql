-- Mock data: 2 users, 1 household, tasks/assignments/completions, and goals/contributions

INSERT INTO users (id, created_at, updated_at, first_name, last_name, avatar_url)
VALUES
    ('11111111-1111-1111-1111-111111111111', now(), now(), 'Terri', 'de Boer', NULL),
    ('22222222-2222-2222-2222-222222222222', now(), now(), 'Alex', 'Janssen', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO households (id, created_at, updated_at, name, type, photo_url, city, zipcode)
VALUES
    ('33333333-3333-3333-3333-333333333333', now(), now(), 'The Home', 'FAMILY', NULL, 'Amsterdam', '1011AB')
ON CONFLICT (id) DO NOTHING;

INSERT INTO household_members (id, created_at, updated_at, household_id, user_id, points)
VALUES
    ('44444444-4444-4444-4444-444444444444', now(), now(), '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 10),
    ('55555555-5555-5555-5555-555555555555', now(), now(), '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', 20)
ON CONFLICT (id) DO NOTHING;

-- Tasks
INSERT INTO tasks (id, created_at, updated_at, title, description, effort, frequency, starting_date, always_same_day, default_points, strategy, responsible_member_id, household_id)
VALUES
    ('66666666-6666-6666-6666-666666666661', now(), now(), 'Do the dishes', 'Wash, dry and put away the dishes', 'SMALL', 'DAILY', CURRENT_DATE - INTERVAL '10 days', false, 5, 'SOLO', '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333'),
    ('66666666-6666-6666-6666-666666666662', now(), now(), 'Vacuum living room', 'Vacuum the living room and hallway', 'MEDIUM', 'WEEKLY', CURRENT_DATE - INTERVAL '10 days', false, 10, 'ROTATING', NULL, '33333333-3333-3333-3333-333333333333'),
    ('66666666-6666-6666-6666-666666666663', now(), now(), 'Take out trash', 'Take the trash and recycling out', 'SMALL', 'EVERY_OTHER_DAY', CURRENT_DATE - INTERVAL '10 days', false, 5, 'SOLO', '55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333')
ON CONFLICT (id) DO NOTHING;

-- Task assignments
INSERT INTO task_assignments (id, created_at, updated_at, household_member_id, task_id, due_date)
VALUES
    ('77777777-7777-7777-7777-777777777771', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666661', CURRENT_DATE - INTERVAL '1 day'),
    ('77777777-7777-7777-7777-777777777772', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666661', CURRENT_DATE),
    ('77777777-7777-7777-7777-777777777773', now(), now(), '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666662', CURRENT_DATE - INTERVAL '2 days'),
    ('77777777-7777-7777-7777-777777777774', now(), now(), '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666663', CURRENT_DATE - INTERVAL '1 day'),
    -- Additional open assignments (no matching task_completions row) for frontend testing
    ('77777777-7777-7777-7777-777777777775', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666661', CURRENT_DATE - INTERVAL '5 days'),
    ('77777777-7777-7777-7777-777777777776', now(), now(), '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666662', CURRENT_DATE - INTERVAL '3 days'),
    ('77777777-7777-7777-7777-777777777777', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666663', CURRENT_DATE - INTERVAL '1 day'),
    ('77777777-7777-7777-7777-777777777778', now(), now(), '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666661', CURRENT_DATE),
    ('77777777-7777-7777-7777-777777777779', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666662', CURRENT_DATE + INTERVAL '1 day'),
    ('77777777-7777-7777-7777-77777777777a', now(), now(), '55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666663', CURRENT_DATE + INTERVAL '1 day'),
    ('77777777-7777-7777-7777-77777777777b', now(), now(), '44444444-4444-4444-4444-444444444444', '66666666-6666-6666-6666-666666666661', CURRENT_DATE + INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Goals
INSERT INTO goals (id, created_at, updated_at, household_id, goal_points, title, description, completion_date, current_points, status)
VALUES
    ('99999999-9999-9999-9999-999999999991', now(), now(), '33333333-3333-3333-3333-333333333333', 50, 'Family Movie Night', 'Earn enough points for a movie night with snacks', NULL, 20, 'ACTIVE'),
    ('99999999-9999-9999-9999-999999999992', now(), now(), '33333333-3333-3333-3333-333333333333', 500, 'Weekend Getaway', 'Save up points towards a weekend trip', NULL, 0, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Task completions (assignment 2, the one due today, is left open/incomplete)
-- The first two completions also count as contributions towards the "Family Movie Night" goal
INSERT INTO task_completions (id, created_at, updated_at, completed_at, rewarded_points, task_assignment_id, completed_by_id, goal_id)
VALUES
    ('88888888-8888-8888-8888-888888888881', now(), now(), now() - INTERVAL '1 day', 5, '77777777-7777-7777-7777-777777777771', '44444444-4444-4444-4444-444444444444', '99999999-9999-9999-9999-999999999991'),
    ('88888888-8888-8888-8888-888888888882', now(), now(), now() - INTERVAL '2 days', 10, '77777777-7777-7777-7777-777777777773', '55555555-5555-5555-5555-555555555555', '99999999-9999-9999-9999-999999999991'),
    ('88888888-8888-8888-8888-888888888883', now(), now(), now() - INTERVAL '1 day', 5, '77777777-7777-7777-7777-777777777774', '55555555-5555-5555-5555-555555555555', NULL)
ON CONFLICT (id) DO NOTHING;
