package ru.nekoguys.game.entity.competition.model

sealed class CompetitionStage(val name: String) {
    object Draft : CompetitionStage("DRAFT")
    object Registration : CompetitionStage("REGISTRATION")
    data class InProgress(val round: Int) : CompetitionStage("IN_PROGRESS")
    object Ended : CompetitionStage("ENDED")

    override fun toString() = name
}
