package ru.nekoguys.game.entity.competition.service

class CompetitionProcessException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalStateException()

fun processError(message: String): Nothing =
    throw CompetitionProcessException(message)
