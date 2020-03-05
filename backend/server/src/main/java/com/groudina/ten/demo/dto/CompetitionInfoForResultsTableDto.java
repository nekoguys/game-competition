package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CompetitionInfoForResultsTableDto implements Serializable {
    private static final long serialVersionUID = -8397730583012650066L;
    private String name;

    private int connectedTeamsCount;

    private int roundsCount;
}
