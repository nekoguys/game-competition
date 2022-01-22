package ru.selemilka.game.process.competition.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_process_infos")
data class DbCompetitionProcessInfo(
    @Id
    var id: Long? = null,
    var gameId: Long,
    var stateId: Long,
)