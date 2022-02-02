CREATE TABLE IF NOT EXISTS users
(
    id       BIGSERIAL PRIMARY KEY,
    email    varchar NOT NULL,
    role     varchar NOT NULL,
    password varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS game_teams
(
    team_id     BIGSERIAL PRIMARY KEY,
    team_number int    NOT NULL,
    game_id     bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS banned_competition_teams
(
    team_id   BIGSERIAL,
    ban_round integer
);

CREATE TABLE IF NOT EXISTS team_members
(
    id        BIGSERIAL PRIMARY KEY,
    team_id   bigint,
    member_id bigint,
    captain   boolean NOT NULL
);

ALTER TABLE team_members
    ADD FOREIGN KEY (team_id) REFERENCES game_teams (team_id);


CREATE TABLE IF NOT EXISTS game_props
(
    id         BIGSERIAL PRIMARY KEY,
    creator_id bigint  NOT NULL,
    game_type  varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS competition_game_props
(
    id                            BIGSERIAL PRIMARY KEY,
    name                          varchar NOT NULL,
    expenses_formula              varchar NOT NULL,
    demand_formula                varchar NOT NULL,
    max_teams_amount              integer NOT NULL,
    max_team_size                 integer NOT NULL,
    rounds_count                  integer NOT NULL,
    round_length_in_seconds       integer NOT NULL,
    team_loss_upperbound          integer NOT NULL,
    instruction                   varchar NOT NULL,
    show_prev_round_results       boolean NOT NULL,
    show_students_results_table   boolean NOT NULL,
    auto_round_ending             boolean NOT NULL,
    end_round_before_all_answered boolean NOT NULL,
    show_other_teams_members      boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS game_sessions
(
    id                 BIGSERIAL PRIMARY KEY,
    props_id           bigint    NOT NULL,
    last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS competition_game_sessions
(
    id         BIGSERIAL PRIMARY KEY,
    stage      varchar NOT NULL,
    last_round integer
);

CREATE TABLE IF NOT EXISTS competition_round_infos
(
    id           BIGSERIAL PRIMARY KEY,
    session_id   bigint,
    round_number integer,
    start_time   timestamp,
    end_time     timestamp,
    is_ended     boolean,
    UNIQUE (id, round_number)
);

CREATE TABLE IF NOT EXISTS competition_round_answers
(
    id       BIGSERIAL PRIMARY KEY,
    round_id bigint NOT NULL,
    value    integer,
    team_id  bigint
);

CREATE TABLE IF NOT EXISTS competition_round_results
(
    id       BIGSERIAL PRIMARY KEY,
    round_id bigint NOT NUll,
    income   decimal,
    team_id  bigint
);

ALTER TABLE game_teams
    ADD CONSTRAINT fk_game_team_game_id
        FOREIGN KEY (game_id) REFERENCES game_sessions (id);

ALTER TABLE team_members
    ADD CONSTRAINT fk_team_members_user_id
        FOREIGN KEY (member_id) REFERENCES users (id);

ALTER TABLE game_props
    ADD CONSTRAINT fk_game_props_creator_id
        FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE game_props
    ADD CONSTRAINT fk_game_props_game_type_check
        CHECK (game_type in ('COMPETITION'));

ALTER TABLE competition_game_props
    ADD CONSTRAINT fk_competition_game_props_id
        FOREIGN KEY (id) REFERENCES game_props (id);

ALTER TABLE competition_game_sessions
    ADD CONSTRAINT fk_competition_game_sessions_id
        FOREIGN KEY (id) REFERENCES game_sessions (id);

ALTER TABLE competition_round_infos
    ADD CONSTRAINT fk_competition_round_infos_session_id
        FOREIGN KEY (session_id) REFERENCES competition_game_sessions (id);

ALTER TABLE competition_game_sessions
    ADD CONSTRAINT competition_game_sessions_stage_check
        CHECK (stage in ('DRAFT', 'REGISTRATION', 'IN_PROGRESS', 'ENDED'));

ALTER TABLE competition_round_answers
    ADD CONSTRAINT fk_competition_round_answers_round_id
        FOREIGN KEY (round_id) REFERENCES competition_round_infos (id);

ALTER TABLE competition_round_answers
    ADD CONSTRAINT fk_competition_round_answers_team_id
        FOREIGN KEY (team_id) REFERENCES game_teams (team_id);

ALTER TABLE competition_round_results
    ADD CONSTRAINT fk_competition_round_results_round_id
        FOREIGN KEY (round_id) REFERENCES competition_round_infos (id);

ALTER TABLE competition_round_results
    ADD CONSTRAINT fk_competition_round_results_team_id
        FOREIGN KEY (team_id) REFERENCES game_teams (team_id);

ALTER TABLE game_sessions
    ADD CONSTRAINT fk_game_sessions_props_id
        FOREIGN KEY (props_id) REFERENCES game_props (id);

ALTER TABLE game_teams
    ADD CONSTRAINT fk_game_team_id_game_teams
        FOREIGN KEY (team_id) REFERENCES game_teams (team_id);

