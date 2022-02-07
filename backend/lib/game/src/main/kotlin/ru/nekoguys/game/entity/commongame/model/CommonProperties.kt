package ru.nekoguys.game.entity.commongame.model

import ru.nekoguys.game.entity.user.model.User

interface CommonProperties {
    data class Id(val number: Long)

    val id: Id
    val creatorId: User.Id
}
