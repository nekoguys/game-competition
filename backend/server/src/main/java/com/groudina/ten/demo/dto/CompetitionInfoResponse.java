package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groudina.ten.demo.models.DbCompetition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionInfoResponse implements Serializable {
    private static final long serialVersionUID = -7097916956509073157L;

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

    private String instruction;

    @JsonProperty("should_show_student_previous_round_results")
    private boolean shouldShowStudentPreviousRoundResults;

    @JsonProperty("should_end_round_before_all_answered")
    private boolean shouldEndRoundBeforeAllAnswered;

    @JsonProperty("should_show_result_table_in_end")
    private boolean shouldShowResultTableInEnd;

    @JsonProperty("last_update_time")
    private LocalDateTime lastUpdateTime;

    private String pin;

    private DbCompetition.State state;
}
