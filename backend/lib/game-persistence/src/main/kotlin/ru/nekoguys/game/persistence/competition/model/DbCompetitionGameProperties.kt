package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("competition_game_props")
data class DbCompetitionGameProperties(
    @Id
    var id: Long? = null,

    var name: String,

    @Column("expenses_formula")
    var expensesFormula: DbExpensesFormula,

    @Column("demand_formula")
    var demandFormula: DbDemandFormula,

    @Column("max_teams_amount")
    var maxTeamsAmount: Int,

    @Column("max_team_size")
    var maxTeamSize: Int,

    @Column("rounds_count")
    var roundsCount: Int,

    @Column("round_length_in_seconds")
    var roundLengthInSeconds: Int,

    @Column("team_loss_upperbound")
    var teamLossLimit: Int,

    var instruction: String,

    @Column("show_prev_round_results")
    var showPreviousRoundResults: Boolean,

    @Column("show_students_results_table")
    var showStudentsResultsTable: Boolean,

    @Column("auto_round_ending")
    var autoRoundEnding: Boolean,

    @Column("show_other_teams_members")
    var showOtherTeamsMembers: Boolean
)

data class DbExpensesFormula(
    val xSquareCoefficient: Double,
    val xCoefficient: Double,
    val freeCoefficient: Double
)

fun DbExpensesFormula.toDbString(): String =
    "${xSquareCoefficient};${xCoefficient};${freeCoefficient}"

fun String.toDbExpensesFormulaOrNull(): DbExpensesFormula? =
    split(";")
        .map { it.toDoubleOrNull() ?: return null }
        .takeIf { it.size == 3 }
        ?.let { (a, b, c) -> DbExpensesFormula(a, b, c) }


data class DbDemandFormula(
    val a: Double,
    val b: Double
)

fun DbDemandFormula.toDbString(): String =
    "${a};${b}"

fun String.toDbDemandFormulaOrNull(): DbDemandFormula? =
    split(";")
        .map { it.toDoubleOrNull() ?: return null }
        .takeIf { it.size == 2 }
        ?.let { (a, b) -> DbDemandFormula(a, b) }
