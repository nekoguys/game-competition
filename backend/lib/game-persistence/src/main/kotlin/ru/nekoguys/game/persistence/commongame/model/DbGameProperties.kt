package ru.nekoguys.game.persistence.commongame.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("GAME_PROPS")
data class DbGameProperties(
    @Id
    val id: Long?,

    val creatorId: Long,

    val gameType: DbGameType,
)

enum class DbGameType {
    COMPETITION,
    UNKNOWN,
}
