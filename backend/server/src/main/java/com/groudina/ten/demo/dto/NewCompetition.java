package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
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
    private Integer maxTeamsAmount;

    @JsonProperty("max_team_size")
    private Integer maxTeamSize;

    @JsonProperty("rounds_count")
    private Integer roundsCount;

    @JsonProperty("round_length")
    private Integer roundLength;

    @JsonProperty("team_loss_upperbound")
    private Double teamLossUpperbound;

    private String instruction;

    @JsonProperty("should_show_student_previous_round_results")
    private Boolean shouldShowStudentPreviousRoundResults;

    @JsonProperty("should_end_round_before_all_answered")
    private Boolean shouldEndRoundBeforeAllAnswered;

    @JsonProperty("should_show_result_table_in_end")
    private Boolean shouldShowResultTableInEnd;
}
