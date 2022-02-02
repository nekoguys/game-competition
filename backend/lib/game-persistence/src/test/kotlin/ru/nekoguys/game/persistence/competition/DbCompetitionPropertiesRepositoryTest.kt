package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProperties
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionPropertiesRepository
import ru.nekoguys.game.persistence.user.model.DbUser
import ru.nekoguys.game.persistence.user.model.DbUserRole
import ru.nekoguys.game.persistence.user.repository.DbUserRepository

@GamePersistenceTest
internal class DbCompetitionPropertiesRepositoryTest @Autowired constructor(
    private val dbUserRepository: DbUserRepository,
    private val dbGamePropertiesRepository: DbGamePropertiesRepository,
    private val dbCompetitionPropertiesRepository: DbCompetitionPropertiesRepository,
) {
    @Test
    fun `insertion and retrieval`(): Unit = runBlocking {
        val user = DbUser(
            id = null,
            email = "test@hse.ru",
            password = "897",
            role = DbUserRole.ADMIN,
        ).let { dbUserRepository.save(it) }

        val props = DbGameProperties(
            id = null,
            creatorId = user.id!!,
            gameType = DbGameType.COMPETITION,
        ).let { dbGamePropertiesRepository.save(it) }

        val competitionProps = DbCompetitionProperties(
            parentId = props.id,
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

        assertThat(dbCompetitionPropertiesRepository.findById(competitionProps.parentId!!))
            .isEqualTo(competitionProps)
    }
}
