package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.user.model.User

data class CompetitionProperties(
    override val id: CommonProperties.Id,
    override val creatorId: User.Id,
    val settings: CompetitionSettings,
) : CommonProperties
