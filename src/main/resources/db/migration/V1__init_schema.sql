-- Baseline schema, generated from the JPA entities as of v0.1.0

CREATE TABLE users (
    id          uuid         NOT NULL,
    created_at  timestamp(6),
    updated_at  timestamp(6),
    auth0_sub   varchar(255) NOT NULL,
    first_name  varchar(50)  NOT NULL,
    last_name   varchar(50)  NOT NULL,
    avatar_url  varchar(1024),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_auth0_sub_key UNIQUE (auth0_sub)
);

CREATE TABLE households (
    id          uuid         NOT NULL,
    created_at  timestamp(6),
    updated_at  timestamp(6),
    name        varchar(50)  NOT NULL,
    type        varchar(255) NOT NULL,
    photo_url   varchar(1024),
    city        varchar(85),
    zipcode     varchar(10),
    CONSTRAINT households_pkey PRIMARY KEY (id),
    CONSTRAINT households_type_check CHECK (type IN ('COUPLE', 'FAMILY', 'SHARED'))
);

CREATE TABLE household_members (
    id            uuid         NOT NULL,
    created_at    timestamp(6),
    updated_at    timestamp(6),
    household_id  uuid         NOT NULL,
    user_id       uuid,
    points        integer      NOT NULL,
    role          varchar(255) NOT NULL,
    left_at       timestamp(6),
    CONSTRAINT household_members_pkey PRIMARY KEY (id),
    CONSTRAINT household_members_household_fk FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT household_members_user_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT household_members_role_check CHECK (role IN ('ADMIN', 'MEMBER'))
);

CREATE TABLE household_invites (
    id             uuid         NOT NULL,
    created_at     timestamp(6),
    updated_at     timestamp(6),
    household_id   uuid         NOT NULL,
    code           varchar(32)  NOT NULL,
    created_by_id  uuid         NOT NULL,
    expires_at     timestamp(6) NOT NULL,
    status         varchar(255) NOT NULL,
    CONSTRAINT household_invites_pkey PRIMARY KEY (id),
    CONSTRAINT household_invites_code_key UNIQUE (code),
    CONSTRAINT household_invites_household_fk FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT household_invites_created_by_fk FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT household_invites_status_check CHECK (status IN ('PENDING', 'REVOKED'))
);

CREATE TABLE tasks (
    id                     uuid         NOT NULL,
    created_at             timestamp(6),
    updated_at             timestamp(6),
    household_id           uuid         NOT NULL,
    title                  varchar(100) NOT NULL,
    description            varchar(500),
    effort                 varchar(255) NOT NULL,
    frequency              varchar(255) NOT NULL,
    starting_date          date         NOT NULL,
    always_same_day        boolean      NOT NULL,
    default_points         integer      NOT NULL,
    strategy               varchar(255) NOT NULL,
    responsible_member_id  uuid,
    CONSTRAINT tasks_pkey PRIMARY KEY (id),
    CONSTRAINT tasks_household_fk FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT tasks_responsible_member_fk FOREIGN KEY (responsible_member_id) REFERENCES household_members (id),
    CONSTRAINT tasks_effort_check CHECK (effort IN ('NONE', 'SMALL', 'MEDIUM', 'BIG')),
    CONSTRAINT tasks_frequency_check CHECK (frequency IN (
        'AS_NEEDED', 'ONCE', 'DAILY', 'EVERY_OTHER_DAY', 'EVERY_THREE_DAYS', 'EVERY_FOUR_DAYS',
        'WEEKLY', 'EVERY_OTHER_WEEK', 'MONTHLY', 'EVERY_TWO_MONTHS', 'EVERY_THREE_MONTHS',
        'EVERY_SIX_MONTHS', 'YEARLY')),
    CONSTRAINT tasks_strategy_check CHECK (strategy IN ('SOLO', 'ROTATING'))
);

CREATE TABLE goals (
    id               uuid         NOT NULL,
    created_at       timestamp(6),
    updated_at       timestamp(6),
    household_id     uuid         NOT NULL,
    title            varchar(255) NOT NULL,
    description      varchar(255),
    goal_points      integer      NOT NULL,
    current_points   integer      NOT NULL,
    completion_date  timestamp(6),
    status           varchar(255) NOT NULL,
    CONSTRAINT goals_pkey PRIMARY KEY (id),
    CONSTRAINT goals_household_fk FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT goals_status_check CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE task_assignments (
    id                   uuid NOT NULL,
    created_at           timestamp(6),
    updated_at           timestamp(6),
    task_id              uuid NOT NULL,
    household_member_id  uuid NOT NULL,
    due_date             date,
    CONSTRAINT task_assignments_pkey PRIMARY KEY (id),
    CONSTRAINT task_assignments_task_fk FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT task_assignments_household_member_fk FOREIGN KEY (household_member_id) REFERENCES household_members (id)
);

CREATE TABLE task_completions (
    id                  uuid         NOT NULL,
    created_at          timestamp(6),
    updated_at          timestamp(6),
    task_assignment_id  uuid         NOT NULL,
    completed_by_id     uuid         NOT NULL,
    goal_id             uuid,
    completed_at        timestamp(6) NOT NULL,
    rewarded_points     integer      NOT NULL,
    CONSTRAINT task_completions_pkey PRIMARY KEY (id),
    CONSTRAINT task_completions_task_assignment_id_key UNIQUE (task_assignment_id),
    CONSTRAINT task_completions_task_assignment_fk FOREIGN KEY (task_assignment_id) REFERENCES task_assignments (id),
    CONSTRAINT task_completions_completed_by_fk FOREIGN KEY (completed_by_id) REFERENCES household_members (id),
    CONSTRAINT task_completions_goal_fk FOREIGN KEY (goal_id) REFERENCES goals (id)
);

-- Postgres does not index foreign keys automatically
CREATE INDEX household_members_household_idx ON household_members (household_id);
CREATE INDEX household_members_user_idx ON household_members (user_id);
CREATE INDEX household_invites_household_idx ON household_invites (household_id);
CREATE INDEX tasks_household_idx ON tasks (household_id);
CREATE INDEX goals_household_idx ON goals (household_id);
CREATE INDEX task_assignments_task_idx ON task_assignments (task_id);
CREATE INDEX task_assignments_household_member_idx ON task_assignments (household_member_id);
CREATE INDEX task_completions_completed_by_idx ON task_completions (completed_by_id);
CREATE INDEX task_completions_goal_idx ON task_completions (goal_id);
