package ru.nekoguys.game.entity.competition.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo

// jackson не умеет десериализовывать объекты-синглтоны из Котлина
// при десериализации просто создаётся ещё один инстанс
// для таких целей лучше использовать kotlinx.serialization, но он не такой гибкий
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class CompetitionStage(val name: String) {
    object Draft : CompetitionStage("Draft") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Draft
    }

    object Registration : CompetitionStage("Registration") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Registration
    }

    data class InProcess(
        val round: Int,
    ) : CompetitionStage("InProcess")

    object Ended : CompetitionStage("Ended") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Ended
    }

    override fun toString() = name
}
