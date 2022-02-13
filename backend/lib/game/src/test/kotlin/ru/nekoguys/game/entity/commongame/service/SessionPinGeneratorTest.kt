package ru.nekoguys.game.entity.commongame.service

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionPinGeneratorTest {

    @MockK
    private lateinit var competitionSessionRepository: CompetitionSessionRepository

    private lateinit var sessionPinGenerator: SessionPinGenerator

    @BeforeAll
    fun before() {
        coEvery {
            competitionSessionRepository.findAll(any(), any())
        } answers {
            firstArg<List<Long>>()
                .map { CompetitionSession.Full(_id = CommonSession.Id(it)) }
        }

        sessionPinGenerator = SessionPinGenerator(competitionSessionRepository)
    }

    @Test
    fun `session id have unique pin`() {
        val sessionIds = (0..20_000L).map { CommonSession.Id(it) }

        val pins = sessionIds
            .map { sessionPinGenerator.convertSessionIdToPin(it) }
            .toSet()

        assertThat(pins)
            .hasSameSizeAs(sessionIds)
    }

    @Test
    fun `id can be encoded and decoded back`(): Unit = runBlocking {
        val sessionIds = (0..20_000L).map { CommonSession.Id(it) }

        val encodedSessionIds = sessionIds
            .associateWith { sessionPinGenerator.convertSessionIdToPin(it) }
            .mapValues { (_, pin) -> sessionPinGenerator.decodeIdFromPin(pin) }

        val isAllEquals = encodedSessionIds
            .all { (id, encodedIds) -> id == encodedIds!! }

        assertThat(isAllEquals)
            .describedAs("All sessionIds are encoded and decoded correctly")
            .isTrue
    }
}
