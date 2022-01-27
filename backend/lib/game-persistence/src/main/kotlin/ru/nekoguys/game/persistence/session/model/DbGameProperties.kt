package ru.nekoguys.game.persistence.session.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("GAME_PROPS")
data class DbGameProperties(
    @Id
    val id: Long?,
    @Column("creator_id")
    val creatorId: Long,
    @Column("game_type")
    val gameType: String,//TODO make enum
    @Column("competition_props_id")
    val competitionPropsId: Long?//TODO make enum in business logic
) {
    constructor(creatorId: Long, gameType: String, competitionPropsId: Long?) :
            this(null, creatorId, gameType, competitionPropsId)
}
