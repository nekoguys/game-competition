package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.entity.competition.model.CompetitionDemandFormula
import ru.nekoguys.game.entity.competition.model.CompetitionExpensesFormula
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
class CompetitionPropertiesRepositoryTest @Autowired constructor(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: UserRepository,
) {

    @Test
    fun `can create`() = transactionalOperator.runBlockingWithRollback {
        val user = userRepository.create(
            email = "test@hse.ru",
            password = "880",
            role = UserRole.Teacher,
        )

        val createdProps = competitionPropertiesRepository.create(
            userId = user.id,
            settings = DEFAULT_COMPETITION_SETTINGS,
        )

        val expectedProps = CompetitionProperties(
            id = createdProps.id,
            creatorId = user.id,
            settings = DEFAULT_COMPETITION_SETTINGS,
        )

        val actualProps = competitionPropertiesRepository.load(createdProps.id)

        assertThat(actualProps)
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .isEqualTo(expectedProps)
    }
}

val DEFAULT_COMPETITION_SETTINGS = CompetitionSettings(
    demandFormula = CompetitionDemandFormula(-1.0, 2.0),
    endRoundBeforeAllAnswered = true,
    expensesFormula = CompetitionExpensesFormula(-2.0, 4.0, -1.0),
    instruction = "There is no instruction",
    isAutoRoundEnding = true,
    maxTeamSize = 4,
    maxTeamsAmount = 5,
    name = "Default competition",
    roundLength = 15,
    roundsCount = 10,
    showOtherTeamsMembers = true,
    showPreviousRoundResults = true,
    showStudentsResultsTable = true,
    teamLossLimit = 999,
)
