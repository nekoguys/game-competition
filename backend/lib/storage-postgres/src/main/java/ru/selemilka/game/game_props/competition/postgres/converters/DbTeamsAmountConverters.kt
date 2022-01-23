package ru.selemilka.game.game_props.competition.postgres.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.selemilka.game.game_props.competition.postgres.model.DbTeamsAmount

@WritingConverter
class DbTeamsAmountWritingConverter: Converter<DbTeamsAmount, Int> {
    override fun convert(source: DbTeamsAmount): Int {
        return source.value
    }
}

@ReadingConverter
class DbTeamsAmountReadingConverter : Converter<Int, DbTeamsAmount> {
    override fun convert(source: Int): DbTeamsAmount {
        return DbTeamsAmount(source)
    }

}
