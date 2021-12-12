package ru.selemilka.game.rps.processors

import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.RpsSession.Id
import ru.selemilka.game.core.base.TypedProcessor
import rps.RpsPlayer
import rps.RpsPlayerCommand
import rps.RpsPlayerMessage
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.storage.RpsGameStateStorage
import ru.selemilka.game.core.rps.storage.SubmitTurnWithResultResponse

class RpsGameProcessor(
    private val gameStateStorage: RpsGameStateStorage
) : TypedProcessor<RpsPlayerCommand.Turn, RpsPlayerMessage> {
    override val actionClass = RpsPlayerCommand.Turn::class

    override suspend fun process(id: RpsSession.Id, action: RpsPlayerCommand.Turn): List<RpsPlayerMessage> {
        val result = gameStateStorage.makeTurn(id, RpsGameStateStorage.Turn(action.initiator.name, decision = action.decision))
        return result.toRpsPlayerReaction(id)
    }
}

fun SubmitTurnWithResultResponse.toRpsPlayerReaction(id: RpsSession.Id): List<RpsPlayerMessage> {
    val (turn, roundResult) = this
    val submitTurnResponse = when (turn) {
        is RpsGameStateStorage.SubmitTurnSuccess -> {
            rps.RpsPlayerMessage.PlayerMadeTurn(
                turn = rps.RpsPlayerCommand.Turn(
                    RpsPlayer(name = turn.turn.player),
                    decision = turn.turn.decision
                )
            )
        }
        is RpsGameStateStorage.TurnAlreadySubmittedError -> rps.RpsPlayerMessage.PlayerTurnFailed(
            scope = RpsPlayerScope(
                RpsPlayer(turn.player)
            ), reason = "some reason"
        )
    }

    val roundResultResponse: RpsPlayerMessage? = roundResult?.toRpsPlayerReaction(id)
    return listOfNotNull(submitTurnResponse, roundResultResponse)
}

private fun RpsGameStateStorage.RoundResult.toRpsPlayerReaction(id: RpsSession.Id): RpsPlayerMessage {
    return when (this) {
        is RpsGameStateStorage.Draw -> rps.RpsPlayerMessage.RoundResult(
            scope = ReactionScope.All(id),
            result = rps.RpsPlayerMessage.RoundResult.Draw
        )
        is RpsGameStateStorage.Winner -> rps.RpsPlayerMessage.RoundResult(
            scope = ReactionScope.All(id),
            result = rps.RpsPlayerMessage.RoundResult.Winner(winner = this.winner)
        )
    }
}
