package ru.selemilka.game.core.rps.process

import ru.selemilka.game.core.rps.storage.RpsGameStateStorage

enum class Turn {
    ROCK,
    PAPER,
    SCISSORS,
}

// --- messages---
sealed interface RoundMessage {

}

class RpsRoundProcessor(
    val gameStateStorage: RpsGameStateStorage,
) {
    suspend fun makeBet(player: RpsPlayer, turn: Turn) {
        val bets: List<Bet> = gameStateStorage.getBets(player.session)
        when {
            bets.size >= 2 || bets.any { it.playerName == player.name } -> announces {

            }
        }

    }
}

data class Bet(
    val playerName: String,
    val turn: Turn,
)
