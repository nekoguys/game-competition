package ru.nekoguys.game.persistence.competition.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProperties

interface DbCompetitionPropertiesRepository
    : CoroutineCrudRepository<DbCompetitionProperties, Long>
