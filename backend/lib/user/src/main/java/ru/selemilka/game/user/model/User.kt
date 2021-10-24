package ru.selemilka.game.user.model

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
)
