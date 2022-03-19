package ru.nekoguys.game.entity.commongame.repository

import ru.nekoguys.game.entity.commongame.model.CommonSession

interface CommonSessionRepository {
    suspend fun updateLastModifiedTime(
        sessionId: CommonSession.Id,
    )
}
