CREATE TABLE user_avatars
(
    user_id      uuid        NOT NULL,
    content_type varchar(50) NOT NULL,
    data         bytea       NOT NULL,
    CONSTRAINT user_avatars_pkey PRIMARY KEY (user_id),
    CONSTRAINT user_avatars_user_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE users ADD COLUMN custom_avatar_updated_at timestamp(6);