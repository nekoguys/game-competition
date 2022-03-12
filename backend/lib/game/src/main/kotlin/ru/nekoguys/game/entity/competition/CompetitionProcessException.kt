package ru.nekoguys.game.entity.competition

class CompetitionProcessException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalStateException()

fun competitionProcessError(message: String): Nothing =
    throw CompetitionProcessException(message)
