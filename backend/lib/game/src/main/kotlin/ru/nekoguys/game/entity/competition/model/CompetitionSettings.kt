package ru.nekoguys.game.entity.competition.model

data class CompetitionSettings(
    val demandFormula: CompetitionDemandFormula,
    val expensesFormula: CompetitionExpensesFormula,
    val instruction: String,
    val isAutoRoundEnding: Boolean,
    val maxTeamSize: Int,
    val maxTeamsAmount: Int,
    val name: String,
    val roundLength: Int,
    val roundsCount: Int,
    val shouldEndRoundBeforeAllAnswered: Boolean,
    val shouldShowResultTableInEnd: Boolean,
    val shouldShowStudentPreviousRoundResults: Boolean,
    val showOtherTeamsMembers: Boolean,
    val teamLossUpperbound: Int,
)

data class CompetitionExpensesFormula(
    val xSquareCoefficient: Double,
    val xCoefficient: Double,
    val freeCoefficient: Double,
)

data class CompetitionDemandFormula(
    val xCoefficient: Double,
    val freeCoefficient: Double,
)
