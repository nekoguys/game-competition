package ru.nekoguys.game.entity.competition.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo

// jackson не умеет десериализовывать объекты-синглтоны из Котлина
// при десериализации просто создаётся ещё один инстанс
// для таких целей лучше использовать kotlinx.serialization, но он не такой гибкий
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed class CompetitionStage(val name: String) {
    object Draft : CompetitionStage("DRAFT") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Draft
    }

    object Registration : CompetitionStage("REGISTRATION") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Registration
    }

    data class InProgress(
        val round: Int,
    ) : CompetitionStage("IN_PROGRESS")

    object Ended : CompetitionStage("ENDED") {
        @JvmStatic
        @JsonCreator
        fun deserialize() = Ended
    }

    override fun toString() = name
}
