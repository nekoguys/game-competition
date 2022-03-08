package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionDemandFormula
import ru.nekoguys.game.entity.competition.model.CompetitionExpensesFormula
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionSettingsRepository
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.DbCompetitionStage
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionSessionRepository
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
class CompetitionSettingsRepositoryTest @Autowired constructor(
    private val competitionSettingsRepository: CompetitionSettingsRepository,
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
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
        val dbGameSession = DbGameSession(
            id = null,
            creatorId = user.id.number,
            gameType = DbGameType.COMPETITION,
        ).let { dbGameSessionRepository.save(it) }
        DbCompetitionSession(
            sessionId = dbGameSession.id,
            stage = DbCompetitionStage.REGISTRATION,
            lastRound = null,
        ).let { dbCompetitionSessionRepository.save(it.asNew()) }

        competitionSettingsRepository.save(
            sessionId = CommonSession.Id(dbGameSession.id!!),
            settings = DEFAULT_COMPETITION_SETTINGS,
        )

        val expectedSettings = DEFAULT_COMPETITION_SETTINGS
        val actualSettings = competitionSettingsRepository
            .load(CommonSession.Id(dbGameSession.id!!))
        assertThat(actualSettings)
            .usingRecursiveComparison()
            .withStrictTypeChecking()
            .isEqualTo(expectedSettings)
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
