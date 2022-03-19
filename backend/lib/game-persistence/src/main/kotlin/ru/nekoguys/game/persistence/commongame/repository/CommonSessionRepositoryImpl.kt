package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.commongame.repository.CommonSessionRepository

@Repository
class CommonSessionRepositoryImpl(
    private val databaseClient: DatabaseClient,
) : CommonSessionRepository {
    override suspend fun updateLastModifiedTime(
        sessionId: CommonSession.Id
    ) {
        databaseClient
            .sql(
                """
                UPDATE game_sessions
                SET last_modified_date = now()
                WHERE id = :sessionId
                """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .then()
            .awaitSingleOrNull()
    }
}
