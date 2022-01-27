package ru.nekoguys.game.persistence.competition

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.persistence.GamePersistenceTest
import ru.nekoguys.game.persistence.competition.model.DbCompetitionState
import ru.nekoguys.game.persistence.competition.repository.DbCompetitionStateRepository

@GamePersistenceTest
internal class DbCompetitionStateRepositoryTest @Autowired constructor(
    private val dbCompetitionStateRepository: DbCompetitionStateRepository
) {
    @Test
    fun `all competition states present`(): Unit = runBlocking {
        for (state in DbCompetitionState.State.values()) {
            assertThat(dbCompetitionStateRepository.findFirstByState(state))
                .isNotNull
        }
    }
}
