package ru.selemilka.game.user.postgres.converters

import io.r2dbc.spi.Row
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole

@ReadingConverter
class UserReadingConverter: Converter<Row, DbUser> {
    override fun convert(source: Row): DbUser? {
        val id = source.get("id", Long::class.javaObjectType)
        val email = source.get("email", String::class.java)
        val stringRole = source.get("role", String::class.java)
        val role = stringRole?.let { DbUserRole.valueOf(it) }
        if (id != null && email != null && role != null) {
            return DbUser(id, email, role)
        }
        return null
    }
}