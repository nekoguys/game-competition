package com.groudina.ten.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Getter
@NoArgsConstructor
public class RoundTeamAnswerCancellationDto extends RoundTeamAnswerDto implements Serializable {
    private static final long serialVersionUID = -6853272732151265893L;

    private CancellationInfoDto cancellationInfo;

    public RoundTeamAnswerCancellationDto(int teamIdInGame, int roundNumber, int teamAnswer, CancellationInfoDto cancellationInfo) {
        super(teamIdInGame, roundNumber, teamAnswer);
        this.cancellationInfo = cancellationInfo;
    }
}
