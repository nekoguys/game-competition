package ru.selemilka.game.postgres.model

data class DbUser(
    var id: Long,
    var email: String,
    var name: String? = null,
    var role: DbUserRole = DbUserRole.UNKNOWN,
)
