package ru.nekoguys.game.entity.competition.rule

import com.fasterxml.jackson.annotation.JsonSubTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompetitionMessageDeserializationTest {

    private val subclassesRegisteredForDeserialization =
        CompetitionMessage::class
            .annotations
            .filterIsInstance<JsonSubTypes>()
            .single()
            .value
            .map { it.value }

    private fun allSubclasses(): List<KClass<out CompetitionMessage>> {
        return CompetitionMessage::class.sealedSubclasses
    }

    @ParameterizedTest
    @MethodSource("allSubclasses")
    fun `subclass is registered for deserialization`(subclass: KClass<out CompetitionMessage>) {
        assertThat(subclassesRegisteredForDeserialization)
            .contains(subclass)
    }
}
