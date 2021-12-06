package ru.selemilka.game.user.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("USERS")
data class DbUser(
    @Id
    var id: Long? = null,

    var email: String,

    var role: DbUserRole,
)
