package ru.selemilka.game.teams.competition.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeamBan

@Repository
interface DbCompetitionTeamBanRepository : CoroutineCrudRepository<DbCompetitionTeamBan, Long>