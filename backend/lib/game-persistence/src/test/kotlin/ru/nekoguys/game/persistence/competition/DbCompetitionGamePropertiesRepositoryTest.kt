package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.competition.model.DbCompetitionGameProperties
import ru.nekoguys.game.persistence.competition.model.DbDemandFormula
import ru.nekoguys.game.persistence.competition.model.DbExpensesFormula
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionGamePropertiesRepository

@GamePersistenceTest
internal class DbCompetitionGamePropertiesRepositoryTest @Autowired constructor(
    private val competitionGamePropertiesRepository: DbCompetitionGamePropertiesRepository,
) {
    @Test
    fun `insertion and retrieval`(): Unit = runBlocking {
        val props = DbCompetitionGameProperties(
            name = "sample name",
            expensesFormula = DbExpensesFormula(
                xSquareCoefficient = 1.0,
                xCoefficient = -2.0,
                freeCoefficient = 20.0
            ),
            demandFormula = DbDemandFormula(
                a = 2000.0,
                b = 10.0
            ),
            maxTeamsAmount = 10,
            maxTeamSize = 3,
            teamLossLimit = 1000,
            instruction = "some sample instruction",
            showOtherTeamsMembers = true,
            showPreviousRoundResults = false,
            showStudentsResultsTable = false,
            autoRoundEnding = true,
            roundsCount = 20,
            roundLengthInSeconds = 200
        ).let { competitionGamePropertiesRepository.save(it) }

        assertThat(competitionGamePropertiesRepository.findById(props.id!!))
            .isEqualTo(props)
    }
}
