package ru.nekoguys.game.persistence

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import ru.nekoguys.game.persistence.competition.model.*

@ComponentScan
@Configuration
@EnableR2dbcRepositories
class GamePersistenceConfig {
    @Bean
    fun customConversion(): R2dbcCustomConversions {
        return R2dbcCustomConversions.of(
            PostgresDialect.INSTANCE,
            listOf(
                DbExpensesFormulaWritingConverter,
                DbExpensesFormulaReadingConverter,
                DbDemandFormulaReadingConverter,
                DbDemandFormulaWritingConverter,
            )
        )
    }
}

@WritingConverter
private object DbExpensesFormulaWritingConverter
    : Converter<DbExpensesFormula, String> by Converter(DbExpensesFormula::toDbString)

@ReadingConverter
private object DbExpensesFormulaReadingConverter
    : Converter<String, DbExpensesFormula> by Converter(String::toDbExpensesFormulaOrNull)

@WritingConverter
private object DbDemandFormulaWritingConverter
    : Converter<DbDemandFormula, String> by Converter(DbDemandFormula::toDbString)

@ReadingConverter
private object DbDemandFormulaReadingConverter
    : Converter<String, DbDemandFormula> by Converter(String::toDbDemandFormulaOrNull)
