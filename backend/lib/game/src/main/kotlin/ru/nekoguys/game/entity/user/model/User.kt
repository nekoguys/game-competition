package ru.nekoguys.game.entity.user.model

data class User(
    val id: Id,
    val email: String,
    val password: String,
    val role: UserRole,
    val firstName: String?,
    val secondName: String?,
) {
    data class Id(val number: Long) {
        override fun toString() = number.toString()
    }

    // Методы toString, equals,
    override fun toString(): String {
        return "User(id=$id, email=$email, password=HIDDEN, role=$role, firstName=$firstName, secondName=$secondName)"
    }
}

sealed interface UserRole {
    val topRoleName: String

    sealed interface Student : UserRole {
        companion object Implementation : Student {
            override val topRoleName = "ROLE_STUDENT"

            override fun toString() = "Student"
        }
    }

    sealed interface Teacher : UserRole {
        companion object Implementation : Student, Teacher {
            override val topRoleName = "ROLE_TEACHER"

            override fun toString() = "Teacher"
        }
    }

    sealed interface Admin : UserRole {
        companion object Implementation : Student, Teacher, Admin {
            override val topRoleName = "ROLE_ADMIN"

            override fun toString() = "Admin"
        }
    }
}

fun String.toUserRole(): UserRole =
    when (this) {
        UserRole.Student.topRoleName -> UserRole.Student
        UserRole.Teacher.topRoleName -> UserRole.Teacher
        UserRole.Admin.topRoleName -> UserRole.Admin
        else -> error("Unknown role $this")
    }
