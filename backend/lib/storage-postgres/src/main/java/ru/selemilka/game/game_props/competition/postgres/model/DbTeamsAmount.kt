package ru.selemilka.game.game_props.competition.postgres.model

data class DbTeamsAmount(val value: Int) {
    init {
        require(value > 0)
    }
}