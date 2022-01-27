package ru.nekoguys.game.persistence.session.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.nekoguys.game.persistence.session.model.DbGameSession

@Repository
interface DbGameSessionsRepository : CoroutineCrudRepository<DbGameSession, Long>
