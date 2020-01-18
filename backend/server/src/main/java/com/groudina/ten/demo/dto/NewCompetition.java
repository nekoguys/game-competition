package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NewCompetition implements Serializable {
    private String name;

    private String state;

    @Size(min=3, max=3, message="Expenses formula should contain 3 values separated by ;")
    @JsonProperty("expenses_formula")
    private List<String> expensesFormula;

    @Size(min=2, max=2, message="Expenses formula should contain 2 values separated by ;")
    @JsonProperty("demand_formula")
    private List<String> demandFormula;

    @JsonProperty("max_teams_amount")
    private int maxTeamsAmount;

    @JsonProperty("max_team_size")
    private int maxTeamSize;

    @JsonProperty("rounds_count")
    private int roundsCount;

    @JsonProperty("round_length")
    private int roundLength;

    private String instruction;

    @JsonProperty("show_prev_results")
    private boolean shouldShowStudentPreviousRoundResults;

    @JsonProperty("should_force_end")
    private boolean shouldEndRoundBeforeAllAnswered;

    @JsonProperty("show_result_table")
    private boolean shouldShowResultTableInEnd;
}
