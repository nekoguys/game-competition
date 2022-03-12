package ru.nekoguys.game.persistence.user.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole

@Table("USERS")
data class DbUser(
    @Id
    var id: Long?,

    var email: String,

    var password: String,

    var role: DbUserRole,

    var firstName: String?,

    var secondName: String?,
)

fun User.toDbUser(): DbUser =
    DbUser(
        id = id.number,
        email = email,
        password = password,
        role = role.toDbUserRole(),
        firstName = firstName,
        secondName = secondName,
    )

fun DbUser.toUserOrNull(): User? {
    return User(
        id = User.Id(id!!),
        email = email,
        password = password,
        role = role.toUserRoleOrNull() ?: return null,
        firstName = firstName,
        secondName = secondName,
    )
}

fun UserRole.toDbUserRole(): DbUserRole =
    when (this) {
        is UserRole.Admin -> DbUserRole.ADMIN
        is UserRole.Teacher -> DbUserRole.TEACHER
        is UserRole.Student -> DbUserRole.STUDENT
    }

fun DbUserRole.toUserRoleOrNull(): UserRole? =
    when (this) {
        DbUserRole.ADMIN -> UserRole.Admin
        DbUserRole.TEACHER -> UserRole.Teacher
        DbUserRole.STUDENT -> UserRole.Student
        DbUserRole.UNKNOWN -> null
    }
