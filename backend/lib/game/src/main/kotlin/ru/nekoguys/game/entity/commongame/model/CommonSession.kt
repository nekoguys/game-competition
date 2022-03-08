package ru.nekoguys.game.entity.commongame.model

interface CommonSession {
    data class Id(val number: Long)

    val id: Id
}
