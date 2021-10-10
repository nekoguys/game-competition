package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompetitionInfoForStudentResultsTableDto implements Serializable {
    private static final long serialVersionUID = 6118066697893175625L;

    private String name;

    private String description;

    private String teamName;

    private int teamIdInGame;

    private boolean shouldShowResultTable;

    private boolean shouldShowResultTableInEnd;

    @JsonProperty("isCaptain")
    private boolean isCaptain;

    private int roundsCount;

    private String strategy;
}
