package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.competition.model.CompetitionDemandFormula
import ru.nekoguys.game.entity.competition.model.CompetitionExpensesFormula
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.GamePersistenceTest

@GamePersistenceTest
class CompetitionPropertiesRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
) {

    private val user: User = runBlocking {
        userRepository.create(
            email = "test@hse.ru",
            password = "880",
            role = UserRole.Teacher,
        )
    }

    @Test
    fun `can create`(): Unit = runBlocking {
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
