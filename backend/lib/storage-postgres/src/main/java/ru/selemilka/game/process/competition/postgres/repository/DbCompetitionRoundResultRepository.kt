package ru.selemilka.game.process.competition.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionRoundResult

interface DbCompetitionRoundResultRepository : CoroutineCrudRepository<DbCompetitionRoundResult, Long>