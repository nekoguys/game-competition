package ru.nekoguys.game.entity.commongame.model

interface CommonSession {
    data class Id(val number: Long) {
        override fun toString(): String = number.toString()
    }

    val id: Id
}
