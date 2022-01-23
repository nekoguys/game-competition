package ru.selemilka.game.game_props.competition.postgres.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.selemilka.game.game_props.competition.postgres.model.DbTeamSize

@WritingConverter
class DbTeamsSizeWritingConverter: Converter<DbTeamSize, Int> {
    override fun convert(source: DbTeamSize): Int {
        return source.value
    }
}

@ReadingConverter
class DbTeamsSizeReadingConverter : Converter<Int, DbTeamSize> {
    override fun convert(source: Int): DbTeamSize? {
        return try {
            DbTeamSize(source)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

}