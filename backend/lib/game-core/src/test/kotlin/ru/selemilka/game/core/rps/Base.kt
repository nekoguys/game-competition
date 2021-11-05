package ru.selemilka.game.core.rps

import ru.selemilka.game.core.base.Action
import ru.selemilka.game.core.base.ActionInitiator
import ru.selemilka.game.core.base.Reaction
import ru.selemilka.game.core.base.ReactionScope


data class RpsPlayer(
    val name: String,
) : ActionInitiator

interface RpsAction : Action<RpsPlayer>

data class RpsPlayerScope(
    override val initiator: RpsPlayer,
) : ReactionScope.Initiator

interface RpsReaction : Reaction<ReactionScope>
