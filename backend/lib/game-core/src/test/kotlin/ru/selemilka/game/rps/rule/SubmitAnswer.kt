package ru.selemilka.game.rps.rule

import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.ResourceLocks
import ru.selemilka.game.rps.RpsGameMessage
import ru.selemilka.game.rps.RpsGameRule
import ru.selemilka.game.rps.model.*
import ru.selemilka.game.rps.storage.RpsRoundStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.buildResponse
import ru.selemilka.game.rps.util.deferCommand

sealed interface SubmitAnswerMessage {
    object Submitted : SubmitAnswerMessage
    data class RoundEnded(val result: RoundResult) : SubmitAnswerMessage

    sealed interface Error : SubmitAnswerMessage
    object AnswerAlreadySubmittedError : Error
}

enum class RoundResult {
    DRAW,
    YOU_WON,
    YOU_LOST,
}

fun SubmitAnswerMessage.toRoot(): RpsMessage.AnswerSubmitted =
    RpsMessage.AnswerSubmitted(this)

@Service
class RpsSubmitAnswerRule(
    private val sessionStorage: RpsSessionStorage,
    private val roundStorage: RpsRoundStorage,
) : RpsGameRule<RpsPlayer.Human, RpsCommand.SubmitAnswer, RpsMessage.AnswerSubmitted> {

    private val resourceLocks: ResourceLocks =
        ResourceLocks(
            shared = sortedSetOf(RpsSessionStorage),
            unique = sortedSetOf(RpsRoundStorage),
        )

    override suspend fun getLocksFor(command: RpsCommand.SubmitAnswer): ResourceLocks =
        resourceLocks

    override suspend fun process(
        player: RpsPlayer.Human,
        command: RpsCommand.SubmitAnswer,
    ): List<RpsGameMessage<RpsMessage.AnswerSubmitted>> {
        val settings = sessionStorage.loadSettings(player.sessionId)
        checkNotNull(settings) { "Session must be created at this point" }

        return submitBet(player, command.turn, settings.maxPlayers)
    }

    private suspend fun submitBet(
        player: RpsPlayer.Human,
        bet: Turn,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsMessage.AnswerSubmitted>> {
        val round = getCurrentRound(player, maxPlayers)
        if (round.answers.any { it.player == player }) {
            return buildResponse {
                player { +SubmitAnswerMessage.AnswerAlreadySubmittedError.toRoot() }
            }
        }

        val updatedAnswers = round.answers + RpsRoundAnswer(player = player, bet = bet)
        val updatedRound = round
            .copy(
                answers = updatedAnswers,
                winner = updatedAnswers
                    .takeIf { it.size == maxPlayers }
                    ?.let(::findWinner),
            )
            .also { roundStorage.saveRound(it) }

        return createResponse(player, updatedRound, maxPlayers)
    }

    private suspend fun getCurrentRound(player: RpsPlayer.Human, maxPlayers: Int): RpsRound {
        val roundInStorage = roundStorage.loadCurrentRound(player.sessionId)

        return when {
            roundInStorage == null -> {
                RpsRound(id = RpsRound.Id(player.sessionId, 1))
            }

            roundInStorage.answers.size == maxPlayers -> {
                val oldId = roundInStorage.id
                RpsRound(id = oldId.copy(number = oldId.number + 1))
            }

            else -> roundInStorage
        }
    }

    private fun createResponse(
        player: RpsPlayer.Human,
        updatedRound: RpsRound,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsMessage.AnswerSubmitted>> {
        val winner = updatedRound.winner
        val answers = updatedRound.answers
        val roundPlayers = answers.map { it.player }
        return buildResponse {
            player { +SubmitAnswerMessage.Submitted.toRoot() }

            if (answers.size == maxPlayers && winner == null) {
                roundPlayers {
                    +SubmitAnswerMessage.RoundEnded(result = RoundResult.DRAW).toRoot()
                }
            }

            if (winner != null) {
                winner {
                    +SubmitAnswerMessage.RoundEnded(result = RoundResult.YOU_WON).toRoot()
                }
                (roundPlayers - winner) {
                    +SubmitAnswerMessage.RoundEnded(result = RoundResult.YOU_LOST).toRoot()
                }

                deferCommand(
                    fromPlayer = RpsPlayer.Internal(player.sessionId),
                    command = RpsCommand.ChangeStage(newStage = RpsStage.GAME_FINISHED),
                )
            }
        }
    }

    private fun findWinner(answers: List<RpsRoundAnswer>): RpsPlayer.Human? {
        for (currentAnswer in answers) {
            val (currentPlayer, currentBet) = currentAnswer
            val otherBets = (answers - currentAnswer).map(RpsRoundAnswer::bet)
            if (otherBets.all { bet -> currentBet beats bet }) {
                return currentPlayer
            }
        }
        return null
    }
}
