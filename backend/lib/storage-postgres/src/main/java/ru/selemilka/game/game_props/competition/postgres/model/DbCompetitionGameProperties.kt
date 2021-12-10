package ru.selemilka.game.game_props.competition.postgres.model

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
    var maxTeamsAmount: DbTeamsAmount,
    @Column("max_team_size")
    var maxTeamSize: DbTeamSize,
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
) {
}