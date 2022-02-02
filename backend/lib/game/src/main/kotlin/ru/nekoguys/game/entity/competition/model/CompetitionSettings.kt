package ru.nekoguys.game.entity.competition.model

data class CompetitionSettings(
    val demandFormula: CompetitionDemandFormula,
    val endRoundBeforeAllAnswered: Boolean,
    val expensesFormula: CompetitionExpensesFormula,
    val instruction: String,
    val isAutoRoundEnding: Boolean,
    val maxTeamSize: Int,
    val maxTeamsAmount: Int,
    val name: String,
    val roundLength: Int,
    val roundsCount: Int,
    val showOtherTeamsMembers: Boolean,
    val showPreviousRoundResults: Boolean,
    val showStudentsResultsTable: Boolean,
    val teamLossLimit: Int,
)

data class CompetitionExpensesFormula(
    val xSquareCoefficient: Double,
    val xCoefficient: Double,
    val freeCoefficient: Double,
)

data class CompetitionDemandFormula(
    val freeCoefficient: Double,
    val xCoefficient: Double,
)
