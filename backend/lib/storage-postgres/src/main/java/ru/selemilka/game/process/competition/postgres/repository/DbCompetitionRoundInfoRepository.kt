package ru.selemilka.game.process.competition.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.process.competition.postgres.model.DbCompetitionRoundInfo

@Repository
interface DbCompetitionRoundInfoRepository : CoroutineCrudRepository<DbCompetitionRoundInfo, Long>