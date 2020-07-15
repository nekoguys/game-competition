package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionCloneInfoResponse implements Serializable {
    private static final long serialVersionUID = -7097916956509073156L;

    private String name;

    @JsonProperty("expenses_formula")
    private String expensesFormula;

    @JsonProperty("demand_formula")
    private String demandFormula;

    @JsonProperty("max_teams_amount")
    private int maxTeamsAmount;

    @JsonProperty("max_team_size")
    private int maxTeamSize;

    @JsonProperty("rounds_count")
    private int roundsCount;

    @JsonProperty("round_length")
    private int roundLength;

    @JsonProperty("team_loss_upperbound")
    private Double teamLossUpperbound;

    private String instruction;

    @JsonProperty("should_show_student_previous_round_results")
    private boolean shouldShowStudentPreviousRoundResults;

    @JsonProperty("should_end_round_before_all_answered")
    private boolean shouldEndRoundBeforeAllAnswered;

    @JsonProperty("should_show_result_table_in_end")
    private boolean shouldShowResultTableInEnd;

    @JsonProperty("is_auto_round_ending")
    private boolean isAutoRoundEnding;
}
