package ru.selemilka.game.core.rps

@JvmInline
value class Session(val number: Long) {
    companion object {
        val DEFAULT = Session(0)
    }
}
