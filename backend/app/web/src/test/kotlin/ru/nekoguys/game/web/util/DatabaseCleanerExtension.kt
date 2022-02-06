package ru.nekoguys.game.web.util

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.junit.jupiter.SpringExtension

class DatabaseCleanerExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        val client = SpringExtension
            .getApplicationContext(context)
            .getBean(DatabaseClient::class.java)

        client.sql("""
            DELETE FROM competition_round_results;
            DELETE FROM competition_round_answers;
            DELETE FROM competition_round_infos;
            DELETE FROM competition_team_members;
            DELETE FROM competition_teams;
            DELETE FROM competition_game_sessions;
            DELETE FROM competition_game_props;
            DELETE FROM game_sessions;
            DELETE FROM game_props;
            DELETE FROM users;
        """.trimIndent()).then().block()
    }
}
