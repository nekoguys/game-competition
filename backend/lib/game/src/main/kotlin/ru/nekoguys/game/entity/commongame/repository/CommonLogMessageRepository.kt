package ru.nekoguys.game.entity.commongame.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.core.session.LoggedGameMessage
import ru.nekoguys.game.entity.commongame.model.CommonSession

interface CommonLogMessageRepository {
    /**
     * Сохраняет игровые сообщения в лог
     * Порядок сообщений сохраняется
     */
    suspend fun saveMessages(
        sessionId: CommonSession.Id,
        messages: List<LoggedGameMessage<*, *>>,
    )

    fun <P, Msg> readAllMessages(
        sessionId: CommonSession.Id,
        playerClass: Class<P>,
        messageClass: Class<Msg>,
    ): Flow<LoggedGameMessage<P, Msg>>
}

