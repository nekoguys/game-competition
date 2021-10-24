package ru.selemilka.game.user.postgres.model

data class DbUser(
    var id: Long,
    var email: String,
    var name: String? = null,
    var role: DbUserRole = DbUserRole.UNKNOWN,
)
