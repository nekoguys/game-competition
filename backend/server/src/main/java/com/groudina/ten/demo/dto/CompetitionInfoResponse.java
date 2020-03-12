package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groudina.ten.demo.models.DbCompetition;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionInfoResponse implements Serializable {
    private static final long serialVersionUID = -7097916956509073157L;

    private String name;

    private String expensesFormula;

    private String demandFormula;

    private int maxTeamsAmount;

    private int maxTeamSize;

    private int roundsCount;

    private int roundLength;

    private String instruction;

    private boolean shouldShowStudentPreviousRoundResults;

    private boolean shouldEndRoundBeforeAllAnswered;

    private boolean shouldShowResultTableInEnd;

    private String lastUpdateTime;

    private String pin;

    private DbCompetition.State state;

    @Setter
    private boolean isOwned;
}
