package ru.nekoguys.game.entity.competition

class CompetitionProcessException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalStateException()

fun String.toCompetitionProcessException() =
    CompetitionProcessException(this)
