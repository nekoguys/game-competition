package ru.nekoguys.game.persistence.commongame.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("GAME_SESSIONS")
data class DbGameSession(
    @Id
    var id: Long?,

    @Column("props_id")
    var propertiesId: Long,
) {
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
}
