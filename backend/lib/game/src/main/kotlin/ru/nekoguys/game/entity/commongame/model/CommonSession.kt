package ru.nekoguys.game.entity.commongame.model

import java.time.LocalDateTime

interface CommonSession<P : CommonProperties> {
    data class Id(val number: Long)

    val id: Id
    val properties: P
    val lastModified: LocalDateTime
}
