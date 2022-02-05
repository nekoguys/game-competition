package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession
import java.time.LocalDateTime

data class CompetitionSession(
    override val id: CommonSession.Id,
    override val properties: CompetitionProperties,
    override val lastModified: LocalDateTime,
    val stage: CompetitionStage,
    val teams: List<CompetitionTeam>,
) : CommonSession<CompetitionProperties>

sealed class CompetitionStage(val name: String) {
    object Draft : CompetitionStage("DRAFT")
    object Registration : CompetitionStage("REGISTRATION")
    data class InProgress(val round: Int) : CompetitionStage("IN_PROGRESS")
    object Ended : CompetitionStage("ENDED")
}
