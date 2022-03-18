package ru.nekoguys.game.entity.competition

class CompetitionProcessException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalStateException()

fun processError(message: String): Nothing =
    throw CompetitionProcessException(message)
