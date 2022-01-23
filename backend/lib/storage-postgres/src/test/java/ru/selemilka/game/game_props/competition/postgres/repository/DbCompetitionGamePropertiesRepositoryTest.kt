package ru.selemilka.game.game_props.competition.postgres.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.reactive.TransactionalOperator
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.game_props.competition.postgres.model.*
import ru.selemilka.game.user.postgres.repository.runBlockingWithRollback

@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal class DbCompetitionGamePropertiesRepositoryTest(
    @Autowired
    val competitionGamePropertiesRepository: DbCompetitionGamePropertiesRepository,
    @Autowired
    val transactionalOperator: TransactionalOperator
) {
    @Test
    fun `insertion and retrieval`() {
        val props = runBlockingWithRollback(transactionalOperator) {
            competitionGamePropertiesRepository.save(
                DbCompetitionGameProperties(
                    name = "sample name",
                    expensesFormula = DbExpensesFormula(1.0, -2.0, 20.0),
                    demandFormula = DbDemandFormula(2000.0, 10.0),
                    maxTeamsAmount = DbTeamsAmount(10),
                    maxTeamSize = DbTeamSize(3),
                    teamLossLimit = 1000,
                    instruction = "some sample instruction",
                    showOtherTeamsMembers = true,
                    showPreviousRoundResults = false,
                    showStudentsResultsTable = false,
                    autoRoundEnding = true,
                    roundsCount = 20,
                    roundLengthInSeconds = 200
                )
            )
        }
        val retrievedProps = runBlocking {
            competitionGamePropertiesRepository.findById(props.id!!)
        }
        assertEquals(props, retrievedProps)
    }
}