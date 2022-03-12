package ru.nekoguys.game.persistence.commongame.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("game_session_logs")
data class DbSessionMessage(
    @Id
    var id: Long?,
    var sessionId: Long,
    var seqNum: Long,
    var players: String, // json-документ
    var message: String, // json-документ
)
