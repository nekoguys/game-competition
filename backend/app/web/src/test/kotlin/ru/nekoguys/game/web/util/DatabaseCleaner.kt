package ru.nekoguys.game.web.util

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class DatabaseCleaner(
    private val databaseClient: DatabaseClient,
) {
    suspend fun clearDatabase() {
        databaseClient.sql(
            """
            DELETE FROM competition_round_answers;
            DELETE FROM competition_round_infos;
            DELETE FROM competition_team_members;
            DELETE FROM competition_teams;
            DELETE FROM competition_game_props;
            DELETE FROM competition_game_sessions;
            DELETE FROM game_session_logs;
            DELETE FROM game_sessions;
            DELETE FROM users;
        """.trimIndent()
        ).then().awaitSingleOrNull()
    }
}
