package ru.nekoguys.game.entity.user.model

data class User(
    val id: Id,
    val email: String,
    val password: String,
    val role: UserRole,
) {
    @JvmInline
    value class Id(val number: Long)
}

sealed interface UserRole {
    sealed interface Student : UserRole {
        companion object Implementation : Student
    }

    sealed interface Teacher : UserRole {
        companion object Implementation : Student, Teacher
    }

    sealed interface Admin : UserRole {
        companion object Implementation : Student, Teacher, Admin
    }
}

