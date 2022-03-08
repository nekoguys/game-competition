CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR NOT NULL,
    role     VARCHAR NOT NULL,
    password VARCHAR NOT NULL
);

CREATE UNIQUE INDEX users_email_unique_index
    ON users (email);

CREATE TABLE game_sessions
(
    id                 BIGSERIAL PRIMARY KEY,
    creator_id         BIGINT  NOT NULL,
    game_type          VARCHAR NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX game_sessions_creator_id_index
    ON game_sessions (creator_id);

CREATE INDEX game_sessions_last_modified_date_index
    ON game_sessions (last_modified_date DESC);

CREATE TABLE competition_game_sessions
(
    id         BIGINT PRIMARY KEY,
    stage      VARCHAR NOT NULL,
    last_round INT
);

CREATE TABLE competition_game_props
(
    id                            BIGINT PRIMARY KEY,
    auto_round_ending             BOOLEAN NOT NULL,
    demand_formula                VARCHAR NOT NULL,
    end_round_before_all_answered BOOLEAN NOT NULL,
    expenses_formula              VARCHAR NOT NULL,
    instruction                   VARCHAR NOT NULL,
    max_team_size                 INT     NOT NULL,
    max_teams_amount              INT     NOT NULL,
    name                          VARCHAR NOT NULL,
    round_length_in_seconds       INT     NOT NULL,
    rounds_count                  INT     NOT NULL,
    show_other_teams_members      BOOLEAN NOT NULL,
    show_prev_round_results       BOOLEAN NOT NULL,
    show_students_results_table   BOOLEAN NOT NULL,
    team_loss_upperbound          INT     NOT NULL
);

CREATE TABLE competition_teams
(
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT  NOT NULL,
    team_number INT     NOT NULL,
    name        VARCHAR NOT NULL,
    password    VARCHAR NOT NULL,
    ban_round   INT     NULL
);

CREATE UNIQUE INDEX competition_teams_session_id_name_unique_index
    ON competition_teams (session_id, name);

CREATE TABLE competition_team_members
(
    id      BIGSERIAL PRIMARY KEY,
    team_id BIGINT,
    user_id BIGINT,
    captain BOOLEAN NOT NULL
);

CREATE TABLE competition_round_infos
(
    id           BIGSERIAL PRIMARY KEY,
    session_id   BIGINT NOT NULL,
    round_number INT,
    start_time   TIMESTAMP,
    end_time     TIMESTAMP,
    is_ended     BOOLEAN,
    UNIQUE (id, round_number)
);

CREATE TABLE competition_round_answers
(
    id       BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL,
    value    INT,
    team_id  BIGINT
);

CREATE TABLE competition_round_results
(
    id       BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NUll,
    income   decimal,
    team_id  BIGINT
);

ALTER TABLE game_sessions
    ADD CONSTRAINT fk_game_sessions_creator_id
        FOREIGN KEY (creator_id) REFERENCES users (id);

ALTER TABLE game_sessions
    ADD CONSTRAINT fk_game_sessions_game_type_check
        CHECK (game_type in ('COMPETITION'));

ALTER TABLE competition_game_sessions
    ADD CONSTRAINT fk_competition_game_sessions_id
        FOREIGN KEY (id) REFERENCES game_sessions (id);

ALTER TABLE competition_game_sessions
    ADD CONSTRAINT competition_game_sessions_stage_check
        CHECK (stage in ('DRAFT', 'REGISTRATION', 'IN_PROGRESS', 'ENDED'));

ALTER TABLE competition_game_props
    ADD CONSTRAINT fk_competition_game_props_id
        FOREIGN KEY (id) REFERENCES competition_game_sessions (id);

ALTER TABLE competition_teams
    ADD CONSTRAINT fk_competition_teams_session_id
        FOREIGN KEY (session_id) REFERENCES game_sessions (id);

ALTER TABLE competition_team_members
    ADD CONSTRAINT fk_competition_team_members_team_id
        FOREIGN KEY (team_id) REFERENCES competition_teams (id);

ALTER TABLE competition_team_members
    ADD CONSTRAINT fk_competition_team_members_user_id
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE competition_round_infos
    ADD CONSTRAINT fk_competition_round_infos_session_id
        FOREIGN KEY (session_id) REFERENCES competition_game_sessions (id);

ALTER TABLE competition_round_answers
    ADD CONSTRAINT fk_competition_round_answers_round_id
        FOREIGN KEY (round_id) REFERENCES competition_round_infos (id);

ALTER TABLE competition_round_answers
    ADD CONSTRAINT fk_competition_round_answers_team_id
        FOREIGN KEY (team_id) REFERENCES competition_teams (id);

ALTER TABLE competition_round_results
    ADD CONSTRAINT fk_competition_round_results_round_id
        FOREIGN KEY (round_id) REFERENCES competition_round_infos (id);

ALTER TABLE competition_round_results
    ADD CONSTRAINT fk_competition_round_results_team_id
        FOREIGN KEY (team_id) REFERENCES competition_teams (id);
