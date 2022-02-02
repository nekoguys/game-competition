package ru.nekoguys.game.entity.commongame.service

import ru.nekoguys.game.entity.commongame.model.CommonSession

interface TypedGameSessionRepository {
    suspend fun load(id: CommonSession.Id): CommonSession<*>
}
