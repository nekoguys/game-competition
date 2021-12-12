package ru.selemilka.game.rps.processor

import ru.selemilka.game.rps.model.*
import ru.selemilka.game.rps.storage.RpsRoundStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage


sealed interface RoundMessage {
    object BetSubmitted : RoundMessage
    data class RoundEnded(val result: RoundResult) : RoundMessage
    data class GameEnded(val winner: String) : RoundMessage

    sealed interface Error : RoundMessage
    object AnswerAlreadySubmittedError : Error
}

enum class RoundResult {
    DRAW,
    YOU_WON,
    YOU_LOST,
}

class RpsRoundProcessor(
    sessionStorage: RpsSessionStorage,
    private val roundStorage: RpsRoundStorage,
) : RpsRootSubProcessor<RpsRootCommand.SubmitAnswer, RpsRootMessage.SubmitAnswer>(sessionStorage) {

    override val expectedStages = setOf(RpsStage.GAME_STARTED)

    override suspend fun process(
        player: RpsPlayer,
        command: RpsRootCommand.SubmitAnswer,
        settings: RpsSessionSettings,
    ): List<RpsResponse<RpsRootMessage>> =
        submitBet(player, command.turn, settings.maxPlayers)
            .map { (player, message) -> RpsResponse(player, RpsRootMessage.SubmitAnswer(message)) }

    private suspend fun submitBet(
        player: RpsPlayer,
        bet: Turn,
        maxPlayers: Int,
    ): List<RpsResponse<RoundMessage>> {
        val currentRound = roundStorage.loadCurrentRound(player.sessionId)
        checkNotNull(currentRound) { "Game must be started" }

        val answers = currentRound.answers

        if (answers.any { it.player == player }) {
            return respond {
                player { +RoundMessage.AnswerAlreadySubmittedError }
            }
        }

        val playerAnswer = RpsRoundAnswer(player = player, bet = bet)
        val updatedRound = currentRound.copy(answers = answers + playerAnswer)
        roundStorage.saveRound(updatedRound)

        return respond {
            player { +RoundMessage.BetSubmitted }

            if (answers.size == maxPlayers) {
                responses += compareAnswersAndEnrichResponse(updatedRound.answers)
            }
        }
    }

    private fun compareAnswersAndEnrichResponse(
        answers: List<RpsRoundAnswer>,
    ): List<RpsResponse<RoundMessage>> {
        val players = answers.map { it.player }
        return when (val winner = findWinner(answers)) {
            null -> respond(players) {
                +RoundMessage.RoundEnded(result = RoundResult.DRAW)
            }

            else -> respond {
                winner {
                    +RoundMessage.RoundEnded(result = RoundResult.YOU_WON)
                }
                (players - winner) {
                    +RoundMessage.RoundEnded(result = RoundResult.YOU_LOST)
                }
            }
        }
    }

    private fun findWinner(answers: List<RpsRoundAnswer>): RpsPlayer? {
        for (currentAnswer in answers) {
            val (currentPlayer, currentBet) = currentAnswer
            if ((answers - currentAnswer).all { (_, bet) -> currentBet beats bet }) {
                return currentPlayer
            }
        }
        return null
    }
}
