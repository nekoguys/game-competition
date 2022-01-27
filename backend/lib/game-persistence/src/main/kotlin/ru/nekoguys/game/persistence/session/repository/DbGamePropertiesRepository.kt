package ru.nekoguys.game.persistence.session.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.nekoguys.game.persistence.session.model.DbGameProperties

@Repository
interface DbGamePropertiesRepository : CoroutineCrudRepository<DbGameProperties, Long>
