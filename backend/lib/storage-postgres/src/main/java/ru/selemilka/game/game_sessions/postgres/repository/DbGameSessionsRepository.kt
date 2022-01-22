package ru.selemilka.game.game_sessions.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.game_sessions.postgres.model.DbGameSession

@Repository
interface DbGameSessionsRepository : CoroutineCrudRepository<DbGameSession, Long>