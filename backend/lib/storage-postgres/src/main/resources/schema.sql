CREATE TYPE user_role AS ENUM (
    'student',
    'teacher',
    'admin'
);

CREATE TABLE users (
    id SERIAL GENERATED ALWAYS AS IDENTITY,
    email TEXT NOT NULL,
    name TEXT,
    role user_role,

    PRIMARY KEY (id)
);

CREATE TABLE game_props (
    props_id SERIAL GENERATED ALWAYS AS IDENTITY,
    creator_id SERIAL NOT NULL,
    game_type TEXT NOT NULL,

    PRIMARY KEY (props_id),
    CONSTRAINT fk_game_props_creator
                        FOREIGN KEY (creator_id)
                        REFERENCES users(id)
);

CREATE TABLE game_sessions (
    id SERIAL GENERATED ALWAYS AS IDENTITY,
    props_id SERIAL NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_game_sessions_id
                           FOREIGN KEY (props_id)
                           REFERENCES game_props(props_id)
);

CREATE TABLE game_actions (
    id SERIAL GENERATED ALWAYS AS IDENTITY,
    props_id SERIAL NOT NULL,
    session_id SERIAL NOT NULL,
    logtime TIMESTAMP NOT NULL,
    action JSON NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_game_actions_props_id
                          FOREIGN KEY (props_id)
                          REFERENCES game_props(props_id),
    CONSTRAINT fk_game_actions_session_id
                          FOREIGN KEY (session_id, props_id)
                          REFERENCES game_sessions(id, props_id)
);
CREATE INDEX idx_game_actions_logtime ON game_actions(logtime);

CREATE TABLE competition_states (
    session_id SERIAL NOT NULL,
    PRIMARY KEY (session_id),
    CONSTRAINT fk_game_states_id
                         FOREIGN KEY (session_id)
                         REFERENCES game_sessions(id)
);
