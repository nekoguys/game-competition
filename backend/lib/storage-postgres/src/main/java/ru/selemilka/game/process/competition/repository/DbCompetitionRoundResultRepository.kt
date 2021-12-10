package ru.selemilka.game.process.competition.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.process.competition.model.DbCompetitionRoundResult

interface DbCompetitionRoundResultRepository : CoroutineCrudRepository<DbCompetitionRoundResult, Long>