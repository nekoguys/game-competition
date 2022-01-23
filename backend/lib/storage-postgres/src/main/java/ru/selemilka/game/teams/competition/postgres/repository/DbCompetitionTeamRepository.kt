package ru.selemilka.game.teams.competition.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeam

interface DbCompetitionTeamRepository : CoroutineCrudRepository<DbCompetitionTeam, Long>
