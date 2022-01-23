package ru.selemilka.game.game_props.competition.postgres.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.selemilka.game.game_props.competition.postgres.model.DbDemandFormula

@WritingConverter
class DbDemandFormulaWritingConverter : Converter<DbDemandFormula, String> {
    override fun convert(source: DbDemandFormula): String {
        return "${source.a};${source.b}"
    }
}

@ReadingConverter
class DbDemandFormulaReadingConverter: Converter<String, DbDemandFormula> {
    override fun convert(source: String): DbDemandFormula? {
        val splitted = source.split(";").map { it.toDoubleOrNull() }
        if (splitted.size != 2 && splitted.all { it != null }) {
            return null
        }
        val (a, b) = splitted.filterNotNull()
        return DbDemandFormula(a, b)
    }
}