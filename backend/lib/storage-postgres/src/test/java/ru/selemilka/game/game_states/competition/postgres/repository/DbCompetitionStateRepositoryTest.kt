package ru.selemilka.game.game_states.competition.postgres.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.game_states.competition.postgres.model.DbCompetitionState

@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal class DbCompetitionStateRepositoryTest(
    @Autowired
    val dbCompetitionStateRepository: DbCompetitionStateRepository
) {
    @Test
    fun `all competition states present`() {
        for (state in DbCompetitionState.State.values()) {
            assertNotNull(runBlocking { dbCompetitionStateRepository.findFirstByState(state) })
        }
    }
}