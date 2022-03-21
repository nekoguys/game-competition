package ru.nekoguys.game.entity.competition.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundResultsCalculatorTest {

    private val settings = CompetitionSettings(
        demandFormula = CompetitionDemandFormula(
            freeCoefficient = 2.0,
            xCoefficient = 1.0,
        ),
        endRoundBeforeAllAnswered = false,
        expensesFormula = CompetitionExpensesFormula(
            xSquareCoefficient = 1.0,
            xCoefficient = 2.0,
            freeCoefficient = 3.0
        ),
        instruction = "instruction",
        isAutoRoundEnding = false,
        maxTeamSize = 4,
        maxTeamsAmount = 4,
        name = "Test competition",
        roundLength = 40,
        roundsCount = 10,
        showOtherTeamsMembers = false,
        showPreviousRoundResults = false,
        showStudentsResultsTable = false,
        teamLossLimit = Int.MAX_VALUE,
    )
    private val sessionId = CommonSession.Id(2)
    private val firstTeamId = CompetitionTeam.Id(3)
    private val secondTeamId = CompetitionTeam.Id(4)
    private val thirdTeamId = CompetitionTeam.Id(5)
    private val roundNumber = 4

    private fun createAnswer(teamId: CompetitionTeam.Id, production: Int) =
        CompetitionRoundAnswer.WithoutIncome(
            sessionId = sessionId,
            teamId = teamId,
            roundNumber = roundNumber,
            production = production
        )

    @Test
    fun `zero price and negative incomes`() {
        val answers = listOf(
            createAnswer(firstTeamId, 1),
            createAnswer(secondTeamId, 2),
        )

        val (incomes, bannedTeamsIds, price) = RoundResultsCalculator
            .calculateResults(settings, answers)

        assertThat(price)
            .isEqualTo(0.0, within(1e-7))
        assertThat(incomes.getValue(firstTeamId))
            .isEqualTo(-1.0 - 2.0 - 3.0, within(1e-7))
        assertThat(incomes.getValue(secondTeamId))
            .isEqualTo(-4.0 - 4.0 - 3.0, within(1e-7))
        assertThat(bannedTeamsIds)
            .isEmpty()
    }

    @Test
    fun `non-zero price, banned team and team without answer`() {
        val customSettings = settings.copy(
            demandFormula = CompetitionDemandFormula(
                freeCoefficient = 100.0,
                xCoefficient = 10.0,
            ),
            teamLossLimit = 500,
        )
        val answers = listOf(
            createAnswer(firstTeamId, 10),
            createAnswer(secondTeamId, 30),
        )

        val (incomes, bannedTeamsIds, price) = RoundResultsCalculator
            .calculateResults(customSettings, answers)

        assertThat(price)
            .isEqualTo(6.0, within(1e-7))
        assertThat(incomes.getValue(firstTeamId))
            .isEqualTo(6.0 * 10 - 10 * 10 - 10 * 2 - 3, within(1e-7))
        assertThat(incomes.getValue(secondTeamId))
            .isEqualTo(6.0 * 30 - 30 * 30 - 30 * 2 - 3, within(1e-7))
        assertThat(incomes.getValue(thirdTeamId))
            .isEqualTo(-3.0, within(1e-7))
        assertThat(bannedTeamsIds)
            .containsExactly(secondTeamId)
    }
}
