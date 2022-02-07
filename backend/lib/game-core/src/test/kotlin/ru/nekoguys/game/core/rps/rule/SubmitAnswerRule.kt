package ru.nekoguys.game.core.rps.rule

import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.ResourceLocks
import ru.nekoguys.game.core.rps.RpsGameMessage
import ru.nekoguys.game.core.rps.RpsGameRule
import ru.nekoguys.game.core.rps.model.*
import ru.nekoguys.game.core.rps.storage.RpsRoundStorage
import ru.nekoguys.game.core.rps.storage.RpsSessionStorage
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.core.util.defer

@Serializable
sealed class RpsSubmitAnswerMessage : RpsMessage() {
    @Serializable
    object Submitted : RpsSubmitAnswerMessage()

    @Serializable
    data class RoundEnded(val result: RoundResult) : RpsSubmitAnswerMessage()
}

@Serializable
sealed class SubmitAnswerMessageError : RpsSubmitAnswerMessage() {
    @Serializable
    object AnswerAlreadySubmitted : SubmitAnswerMessageError()
}

enum class RoundResult {
    DRAW,
    YOU_WON,
    YOU_LOST,
}

@Service
class RpsSubmitAnswerRule(
    private val sessionStorage: RpsSessionStorage,
    private val roundStorage: RpsRoundStorage,
) : RpsGameRule<RpsPlayer.Human, RpsCommand.SubmitAnswer, RpsSubmitAnswerMessage> {

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
    ): List<RpsGameMessage<RpsSubmitAnswerMessage>> {
        val settings = sessionStorage.loadSettings(player.sessionId)
        checkNotNull(settings) { "Session must be created at this point" }

        return submitBet(player, command.turn, settings.maxPlayers)
    }

    private suspend fun submitBet(
        player: RpsPlayer.Human,
        bet: Turn,
        maxPlayers: Int,
    ): List<RpsGameMessage<RpsSubmitAnswerMessage>> {
        val round = getCurrentRound(player, maxPlayers)
        if (round.answers.any { it.player == player }) {
            return buildResponse {
                player { +SubmitAnswerMessageError.AnswerAlreadySubmitted }
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
    ): List<RpsGameMessage<RpsSubmitAnswerMessage>> {
        val winner = updatedRound.winner
        val answers = updatedRound.answers
        val roundPlayers = answers.map { it.player }
        return buildResponse {
            player { +RpsSubmitAnswerMessage.Submitted }

            if (answers.size == maxPlayers && winner == null) {
                roundPlayers {
                    +RpsSubmitAnswerMessage.RoundEnded(result = RoundResult.DRAW)
                }
            }

            if (winner != null) {
                winner {
                    +RpsSubmitAnswerMessage.RoundEnded(result = RoundResult.YOU_WON)
                }
                (roundPlayers - winner) {
                    +RpsSubmitAnswerMessage.RoundEnded(result = RoundResult.YOU_LOST)
                }

                defer(
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
