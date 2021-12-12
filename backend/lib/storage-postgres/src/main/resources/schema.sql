CREATE TABLE IF NOT EXISTS "users"
(
    "id"    BIGSERIAL PRIMARY KEY,
    "email" varchar NOT NULL,
    "role"  varchar NOT NULL
);

CREATE TABLE  IF NOT EXISTS "game_teams"
(
    "team_id"     BIGSERIAL PRIMARY KEY,
    "team_number" int    NOT NULL,
    "game_id"     bigint NOT NULL,
    "ban_round"   integer
);

CREATE TABLE IF NOT EXISTS "team_members"
(
    "team_id"   bigint,
    "member_id" bigint,
    "captain"   boolean NOT NULL,
    PRIMARY KEY ("team_id", "member_id")
);

ALTER TABLE "team_members"
    ADD FOREIGN KEY ("team_id") REFERENCES "game_teams" ("team_id");


CREATE TABLE IF NOT EXISTS "competition_game_states"
(
    "id"   BIGSERIAL PRIMARY KEY,
    "name" varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS "game_props"
(
    "id"                   BIGSERIAL PRIMARY KEY,
    "creator_id"           bigint  NOT NULL,
    "game_type"            varchar NOT NULL,
    "competition_props_id" bigint
);

CREATE TABLE IF NOT EXISTS "competition_game_props"
(
    "id"                          BIGSERIAL PRIMARY KEY,
    "name"                        varchar not null,
    "expenses_formula"            varchar not null,
    "demand_formula"              varchar not null,
    "max_teams_amount"            integer not null,
    "max_team_size"               integer not null,
    "rounds_count"                integer not null,
    "round_length_in_seconds"     integer not null,
    "team_loss_upperbound"        integer not null,
    "instruction"                 varchar not null,
    "show_prev_round_results"     boolean not null,
    "show_students_results_table" boolean not null,
    "auto_round_ending"           boolean not null,
    "show_other_teams_members"    boolean not null
);

CREATE TABLE IF NOT EXISTS "competition_process_infos"
(
    "id"       BIGSERIAL PRIMARY KEY,
    "game_id"  bigint,
    "state_id" bigint
);

CREATE TABLE IF NOT EXISTS "competition_round_infos"
(
    "id"           BIGSERIAL,
    "process_id"   bigint,
    "round_number" integer,
    "start_time"   timestamp,
    "end_time"     timestamp,
    "is_ended"     boolean,
    PRIMARY KEY ("id", "round_number")
);

CREATE TABLE IF NOT EXISTS "competition_round_answers"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "round_info_id" bigint,
    "value"         integer,
    "team_id"       bigint
);

CREATE TABLE IF NOT EXISTS "competition_round_results"
(
    "id"            BIGSERIAL PRIMARY KEY,
    "round_info_id" bigint,
    "income"        decimal,
    "team_id"       bigint
);

CREATE TABLE IF NOT EXISTS "competition_messages"
(
    "id"              BIGSERIAL PRIMARY KEY,
    "process_game_id" bigint,
    "message"         varchar,
    "send_time"       timestamp
);

CREATE TABLE IF NOT EXISTS "game_sessions"
(
    "id"       BIGSERIAL PRIMARY KEY,
    "props_id" bigint
);

ALTER TABLE "game_teams"
    ADD FOREIGN KEY ("game_id") REFERENCES "game_sessions" ("id");

ALTER TABLE "team_members"
    ADD FOREIGN KEY ("member_id") REFERENCES "users" ("id");

ALTER TABLE "game_props"
    ADD FOREIGN KEY ("creator_id") REFERENCES "users" ("id");

ALTER TABLE "game_props"
    ADD FOREIGN KEY ("competition_props_id") REFERENCES "competition_game_props" ("id");

ALTER TABLE "competition_process_infos"
    ADD FOREIGN KEY ("game_id") REFERENCES "game_sessions" ("id");

ALTER TABLE "competition_process_infos"
    ADD FOREIGN KEY ("state_id") REFERENCES "competition_game_states" ("id");

ALTER TABLE "competition_round_infos"
    ADD FOREIGN KEY ("process_id") REFERENCES "competition_process_infos" ("id");

ALTER TABLE "competition_round_answers"
    ADD FOREIGN KEY ("round_info_id") REFERENCES "competition_round_infos" ("id");

ALTER TABLE "competition_round_answers"
    ADD FOREIGN KEY ("team_id") REFERENCES "game_teams" ("team_id");

ALTER TABLE "competition_round_results"
    ADD FOREIGN KEY ("round_info_id") REFERENCES "competition_round_infos" ("id");

ALTER TABLE "competition_round_results"
    ADD FOREIGN KEY ("team_id") REFERENCES "game_teams" ("team_id");

ALTER TABLE "competition_messages"
    ADD FOREIGN KEY ("process_game_id") REFERENCES "competition_process_infos" ("id");

ALTER TABLE "game_sessions"
    ADD FOREIGN KEY ("props_id") REFERENCES "game_props" ("id");

ALTER TABLE "competition_game_states"
    ADD CONSTRAINT state_name_check
    CHECK (
        name in ('DRAFT', 'REGISTRATION', 'IN_PROCESS', 'ENDED')
    );

