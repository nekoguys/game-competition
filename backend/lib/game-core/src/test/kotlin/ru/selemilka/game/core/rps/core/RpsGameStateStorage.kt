package ru.selemilka.game.core.rps.core

import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.rps.RockPaperScissorsTurn

interface RpsGameStateStorage {
    sealed interface SubmitTurnResponse
    sealed interface SubmitTurnError : SubmitTurnResponse
    data class TurnAlreadySubmittedError(val player: String) : SubmitTurnError
    data class SubmitTurnSuccess(val turn: Turn) : SubmitTurnResponse
    data class Turn(val player: String, val turn: RockPaperScissorsTurn)

    sealed interface RoundResult
    data class Winner(val winner: String) : RoundResult
    object Draw : RoundResult

    fun makeTurn(sessionId: SessionId, turn: Turn): SubmitTurnWithResultResponse
}

typealias SubmitTurnWithResultResponse = Pair<RpsGameStateStorage.SubmitTurnResponse, RpsGameStateStorage.RoundResult?>

class RpsGameStateInMemoryStorage : RpsGameStateStorage {
    private class GameState {
        private val answers = mutableListOf<RpsGameStateStorage.Turn>()

        fun submit(answer: RpsGameStateStorage.Turn): SubmitTurnWithResultResponse {
            return if (answers.size >= 2 || answers.any { it.player == answer.player }) {
                Pair(RpsGameStateStorage.TurnAlreadySubmittedError(player = answer.player), null)
            } else {
                answers.add(answer)
                val result: RpsGameStateStorage.RoundResult? = if (answers.size == 2) {
                    if (answers[0].turn.beats(answers[1].turn)) {
                        RpsGameStateStorage.Winner(winner = answers[0].player)
                    } else if (answers[1].turn.beats(answers[0].turn)) {
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

    private val storage = mutableMapOf<SessionId, GameState>()

    override fun makeTurn(
        sessionId: SessionId,
        turn: RpsGameStateStorage.Turn
    ): SubmitTurnWithResultResponse {
        val gameState = storage.computeIfAbsent(sessionId) { GameState() }
        return gameState.submit(turn)
    }
}

fun RockPaperScissorsTurn.beats(another: RockPaperScissorsTurn): Boolean {
    return (this == RockPaperScissorsTurn.Rock && another == RockPaperScissorsTurn.Scissors) ||
            (this == RockPaperScissorsTurn.Scissors && another == RockPaperScissorsTurn.Paper) ||
            (this == RockPaperScissorsTurn.Paper && another == RockPaperScissorsTurn.Rock)
}