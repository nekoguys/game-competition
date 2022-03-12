package ru.nekoguys.game.persistence.competition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.reactive.TransactionalOperator
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProperties
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.DbCompetitionStage
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionPropertiesRepository
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionSessionRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository
import ru.nekoguys.game.persistence.utils.runBlockingWithRollback

@GamePersistenceTest
internal class DbCompetitionSettingsRepositoryTest @Autowired constructor(
    private val dbCompetitionPropertiesRepository: DbCompetitionPropertiesRepository,
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbUserRepository: DbUserRepository,
    private val transactionalOperator: TransactionalOperator,
) {
    @Test
    fun `insertion and retrieval`() = transactionalOperator.runBlockingWithRollback {
        val user = DbUser(
            id = null,
            email = "test-competition-properties@hse.ru",
            password = "897",
            role = DbUserRole.ADMIN,
            firstName = null,
            secondName = null,
        ).let { dbUserRepository.save(it) }
        val dbGameSession = DbGameSession(
            id = null,
            creatorId = user.id!!,
            gameType = DbGameType.COMPETITION,
        ).let { dbGameSessionRepository.save(it) }
        DbCompetitionSession(
            sessionId = dbGameSession.id,
            stage = DbCompetitionStage.REGISTRATION,
            lastRound = null,
        ).let { dbCompetitionSessionRepository.save(it.asNew()) }

        val competitionProps = DbCompetitionProperties(
            sessionId = dbGameSession.id,
            autoRoundEnding = true,
            demandFormula = "2;-4",
            endRoundBeforeAllAnswered = true,
            expensesFormula = "1;-2;3",
            instruction = "some sample instruction",
            maxTeamSize = 3,
            maxTeamsAmount = 10,
            name = "sample name",
            roundLengthInSeconds = 200,
            roundsCount = 20,
            showOtherTeamsMembers = true,
            showPreviousRoundResults = false,
            showStudentsResultsTable = false,
            teamLossLimit = 1000,
        ).let { dbCompetitionPropertiesRepository.save(it.asNew()) }

        assertThat(dbCompetitionPropertiesRepository.findById(competitionProps.sessionId!!))
            .isEqualTo(competitionProps)
    }
}
