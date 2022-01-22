package ru.selemilka.game.game_props.competition.postgres.model

data class DbExpensesFormula(
    val xSquareCoefficient: Double,
    val xCoefficient: Double,
    val freeCoefficient: Double
)