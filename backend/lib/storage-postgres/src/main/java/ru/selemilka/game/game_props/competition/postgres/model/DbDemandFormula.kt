package ru.selemilka.game.game_props.competition.postgres.model

// Q = a - b*price
data class DbDemandFormula(
    val a: Double,
    val b: Double
)