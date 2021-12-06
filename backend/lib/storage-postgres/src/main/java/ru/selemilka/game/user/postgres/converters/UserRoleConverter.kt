package ru.selemilka.game.user.postgres.converters

import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import ru.selemilka.game.user.postgres.model.DbUserRole

@WritingConverter
class UserRoleConverter : EnumWriteSupport<DbUserRole>()