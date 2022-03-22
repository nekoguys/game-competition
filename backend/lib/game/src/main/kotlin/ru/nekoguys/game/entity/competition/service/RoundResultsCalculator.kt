package ru.nekoguys.game.entity.competition.service

import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

object RoundResultsCalculator {
    fun calculateResults(
        settings: CompetitionSettings,
        roundAnswers: List<CompetitionRoundAnswer>,
    ): RoundResults {
        val price = calculatePrice(settings, roundAnswers)
        val teamLossLimit = settings.teamLossLimit
        val defaultIncome = -getTotalCosts(settings, 0)

        val bannedTeamIds = mutableListOf<CompetitionTeam.Id>()
        val incomes = mutableMapOf<CompetitionTeam.Id, Double>()

        for (answer in roundAnswers) {
            val totalCost = getTotalCosts(settings, answer.production)
            val income = answer.production * price - totalCost
            incomes[answer.teamId] = income

            if (income < -teamLossLimit) {
                bannedTeamIds += answer.teamId
            }
        }

        return RoundResults(
            results = incomes,
            bannedTeamIds = bannedTeamIds,
            price = price,
            defaultIncome = defaultIncome,
        )
    }

    private fun calculatePrice(
        settings: CompetitionSettings,
        roundAnswers: List<CompetitionRoundAnswer>,
    ): Double {
        val totalProduction = roundAnswers.sumOf { it.production }

        // production = a - b * price
        // price = (a - production) / b
        val result = with(settings.demandFormula) {
            (freeCoefficient - totalProduction) / xCoefficient
        }
        return if (result.isFinite() && result > 0.0) {
            return result
        } else {
            0.0
        }
    }

    private fun getTotalCosts(
        settings: CompetitionSettings,
        production: Int,
    ): Double =
        with(settings.expensesFormula) {
            xSquareCoefficient * production * production +
                    xCoefficient * production +
                    freeCoefficient
        }
}

data class RoundResults(
    val results: Map<CompetitionTeam.Id, Double>,
    val bannedTeamIds: List<CompetitionTeam.Id>,
    val price: Double,
    val defaultIncome: Double,
)
