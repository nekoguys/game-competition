package ru.selemilka.game.game_props.competition.postgres.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import ru.selemilka.game.game_props.competition.postgres.model.DbExpensesFormula

@WritingConverter
class DbExpensesFormulaWritingConverter : Converter<DbExpensesFormula, String> {
    override fun convert(source: DbExpensesFormula): String {
        return "${source.xSquareCoefficient};${source.xCoefficient};${source.freeCoefficient}"
    }
}

@ReadingConverter
class DbExpensesFormulaReadingConverter : Converter<String, DbExpensesFormula> {
    override fun convert(source: String): DbExpensesFormula? {
        val splitted = source.split(";").map { it.toDoubleOrNull() }
        if (splitted.size != 3 && splitted.all { it != null }) {
            return null
        }
        val (xSquareCoef, xCoef, freeCoef) = splitted.filterNotNull()
        return DbExpensesFormula(
            xSquareCoef,
            xCoef,
            freeCoef
        )
    }

}