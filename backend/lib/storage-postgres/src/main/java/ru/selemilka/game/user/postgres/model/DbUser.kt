package ru.selemilka.game.user.postgres.model

import org.springframework.data.annotation.Id

data class DbUser(
    @Id
    var id: Long,

    var email: String?,

    var role: String?,
)
