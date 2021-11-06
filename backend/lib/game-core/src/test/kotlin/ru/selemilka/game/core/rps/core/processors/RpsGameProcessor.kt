package ru.selemilka.game.core.rps.core.processors

import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import ru.selemilka.game.core.rps.RpsPlayer
import ru.selemilka.game.core.rps.RpsPlayerAction
import ru.selemilka.game.core.rps.RpsPlayerReaction
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.core.RpsGameStateStorage
import ru.selemilka.game.core.rps.core.SubmitTurnWithResultResponse

class RpsGameProcessor(
    private val gameStateStorage: RpsGameStateStorage
) : TypedProcessor<RpsPlayerAction.Turn, RpsPlayerReaction> {
    override val actionClass = RpsPlayerAction.Turn::class

    override suspend fun process(id: SessionId, action: RpsPlayerAction.Turn): List<RpsPlayerReaction> {
        val result = gameStateStorage.makeTurn(id, RpsGameStateStorage.Turn(action.initiator.name, turn = action.turn))
        return result.toRpsPlayerReaction(id)
    }
}

fun SubmitTurnWithResultResponse.toRpsPlayerReaction(id: SessionId): List<RpsPlayerReaction> {
    val (turn, roundResult) = this
    val submitTurnResponse = when (turn) {
        is RpsGameStateStorage.SubmitTurnSuccess -> {
            RpsPlayerReaction.PlayerMadeTurn(
                turn = RpsPlayerAction.Turn(
                    RpsPlayer(name = turn.turn.player),
                    turn = turn.turn.turn
                )
            )
        }
        is RpsGameStateStorage.TurnAlreadySubmittedError -> RpsPlayerReaction.PlayerTurnFailed(
            scope = RpsPlayerScope(
                RpsPlayer(turn.player)
            ), reason = "some reason"
        )
    }

    val roundResultResponse: RpsPlayerReaction? = roundResult?.toRpsPlayerReaction(id)
    return listOfNotNull(submitTurnResponse, roundResultResponse)
}

private fun RpsGameStateStorage.RoundResult.toRpsPlayerReaction(id: SessionId): RpsPlayerReaction {
    return when (this) {
        is RpsGameStateStorage.Draw -> RpsPlayerReaction.RoundResult(
            scope = ReactionScope.All(id),
            result = RpsPlayerReaction.RoundResult.Draw
        )
        is RpsGameStateStorage.Winner -> RpsPlayerReaction.RoundResult(
            scope = ReactionScope.All(id),
            result = RpsPlayerReaction.RoundResult.Winner(winner = this.winner)
        )
    }
}