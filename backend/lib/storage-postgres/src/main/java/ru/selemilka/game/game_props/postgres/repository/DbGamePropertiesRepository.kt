package ru.selemilka.game.game_props.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.game_props.postgres.model.DbGameProperties

@Repository
interface DbGamePropertiesRepository : CoroutineCrudRepository<DbGameProperties, Long> {
}