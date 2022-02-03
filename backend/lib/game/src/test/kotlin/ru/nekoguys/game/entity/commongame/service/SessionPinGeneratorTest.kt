package ru.nekoguys.game.entity.commongame.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.GameEntityTest
import ru.nekoguys.game.entity.commongame.model.CommonSession

@GameEntityTest
class SessionPinGeneratorTest @Autowired constructor(
    private val sessionPinGenerator: SessionPinGenerator,
) {
    @Test
    fun `session id have unique pin`() {
        val sessionIds = (0..100_000L).map { CommonSession.Id(it) }

        val pins = sessionIds
            .map { sessionPinGenerator.convertSessionIdToPin(it) }
            .toSet()

        assertThat(pins)
            .hasSameSizeAs(sessionIds)
    }

    @Test
    fun `id can be encoded and decoded back`() {
        val sessionIds = (0..100_000L).map { CommonSession.Id(it) }

        val encodedSessionIds = sessionIds
            .associateWith { sessionPinGenerator.convertSessionIdToPin(it) }
            .mapValues { (_, pin) -> sessionPinGenerator.decodeIdFromPin(pin) }

        val isAllEquals = encodedSessionIds
            .all { (id, encodedIds) -> id.number == encodedIds }

        assertThat(isAllEquals)
            .describedAs("All sessionIds are encoded and decoded correctly")
            .isTrue
    }
}
