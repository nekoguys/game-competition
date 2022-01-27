package ru.nekoguys.game.persistence.session.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("GAME_SESSIONS")
data class DbGameSession(
    @Id
    var id: Long?,
    @Column("props_id")
    var propertiesId: Long,
) {
    constructor(propertiesId: Long): this(null, propertiesId)
}
