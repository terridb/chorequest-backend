-- IANA time zone per household, so "today" and due dates follow the household's local calendar instead of the server's

ALTER TABLE households
    ADD COLUMN time_zone varchar(64) NOT NULL DEFAULT 'Europe/Amsterdam';
