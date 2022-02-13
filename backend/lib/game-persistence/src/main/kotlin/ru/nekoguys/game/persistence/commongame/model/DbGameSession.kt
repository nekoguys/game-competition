package ru.nekoguys.game.persistence.commongame.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("GAME_SESSIONS")
data class DbGameSession(
    @Id
    var id: Long?,

    var creatorId: Long,

    var gameType: DbGameType,
) {
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
}

enum class DbGameType {
    COMPETITION,
    UNKNOWN,
}
