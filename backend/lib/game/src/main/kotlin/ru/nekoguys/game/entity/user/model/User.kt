package ru.nekoguys.game.entity.user.model

data class User(
    val id: Id,
    val email: String,
    val password: String,
    val role: UserRole,
) {
    data class Id(val number: Long)

    override fun toString(): String {
        return "User(id=$id, email='$email', password='HIDDEN', role=$role)"
    }
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

