package ru.selemilka.game.process.competition.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.process.competition.model.DbCompetitionProcessInfo

@Repository
interface DbCompetitionProcessInfoRepository : CoroutineCrudRepository<DbCompetitionProcessInfo, Long>