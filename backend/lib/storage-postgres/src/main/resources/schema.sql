
CREATE TABLE IF NOT EXISTS users (
    id INTEGER GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    role VARCHAR(64) CHECK (role in ('STUDENT', 'TEACHER', 'ADMIN')),

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS game_props (
    props_id INTEGER GENERATED ALWAYS AS IDENTITY,
    creator_id INTEGER NOT NULL,
    game_type TEXT NOT NULL,

    PRIMARY KEY (props_id),
    CONSTRAINT fk_game_props_creator
                        FOREIGN KEY (creator_id)
                        REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS game_sessions (
    id INTEGER GENERATED ALWAYS AS IDENTITY,
    props_id INTEGER NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_game_sessions_id
                           FOREIGN KEY (props_id)
                           REFERENCES game_props(props_id)
);

CREATE TABLE IF NOT EXISTS game_actions (
    id INTEGER GENERATED ALWAYS AS IDENTITY,
    props_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
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
CREATE INDEX IF NOT EXISTS idx_game_actions_logtime ON game_actions(logtime);

CREATE TABLE IF NOT EXISTS competition_states (
    session_id INTEGER NOT NULL,
    PRIMARY KEY (session_id),
    CONSTRAINT fk_game_states_id
                         FOREIGN KEY (session_id)
                         REFERENCES game_sessions(id)
);
