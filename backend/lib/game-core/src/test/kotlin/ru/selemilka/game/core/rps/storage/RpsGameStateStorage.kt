package ru.selemilka.game.core.rps.storage

import ru.selemilka.game.core.rps.RpsTurn
import ru.selemilka.game.core.rps.Session

interface RpsGameStateStorage {
    sealed interface SubmitTurnResponse
    sealed interface SubmitTurnError : SubmitTurnResponse
    data class TurnAlreadySubmittedError(val player: String) : SubmitTurnError
    data class SubmitTurnSuccess(val turn: Turn) : SubmitTurnResponse
    data class Turn(val player: String, val decision: RpsTurn)

    sealed interface RoundResult
    data class Winner(val winner: String) : RoundResult
    object Draw : RoundResult

    fun isGameEnded(sessionId: Session): Boolean
    fun makeTurn(sessionId: Session, turn: Turn): SubmitTurnWithResultResponse
}

typealias SubmitTurnWithResultResponse =
        Pair<RpsGameStateStorage.SubmitTurnResponse, RpsGameStateStorage.RoundResult?>

class RpsGameStateInMemoryStorage : RpsGameStateStorage {
    private class GameState {
        private val answers = mutableListOf<RpsGameStateStorage.Turn>()

        fun isGameEnded(): Boolean {
            return answers.size == 2
        }

        fun submit(answer: RpsGameStateStorage.Turn): SubmitTurnWithResultResponse {
            return if (answers.size >= 2 || answers.any { it.player == answer.player }) {
                Pair(RpsGameStateStorage.TurnAlreadySubmittedError(player = answer.player), null)
            } else {
                answers.add(answer)
                val result: RpsGameStateStorage.RoundResult? = if (answers.size == 2) {
                    if (answers[0].decision.beats(answers[1].decision)) {
                        RpsGameStateStorage.Winner(winner = answers[0].player)
                    } else if (answers[1].decision.beats(answers[0].decision)) {
                        RpsGameStateStorage.Winner(winner = answers[1].player)
                    } else {
                        RpsGameStateStorage.Draw
                    }
                } else {
                    null
                }

                SubmitTurnWithResultResponse(RpsGameStateStorage.SubmitTurnSuccess(turn = answer), result)
            }
        }
    }

    private val storage = mutableMapOf<Session, GameState>()

    override fun makeTurn(
        sessionId: Session,
        turn: RpsGameStateStorage.Turn,
    ): SubmitTurnWithResultResponse {
        val gameState = storage.computeIfAbsent(sessionId) { GameState() }
        return gameState.submit(turn)
    }

    override fun isGameEnded(sessionId: Session): Boolean {
        return storage[sessionId]?.isGameEnded() ?: false
    }
}

fun RpsTurn.beats(another: RpsTurn): Boolean {
    return when (this) {
        RpsTurn.ROCK -> another == RpsTurn.SCISSORS
        RpsTurn.SCISSORS -> another == RpsTurn.PAPER
        RpsTurn.PAPER -> another == RpsTurn.ROCK
    }
}
