package ru.nekoguys.game.persistence.commongame.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties

interface DbGamePropertiesRepository : CoroutineCrudRepository<DbGameProperties, Long>
