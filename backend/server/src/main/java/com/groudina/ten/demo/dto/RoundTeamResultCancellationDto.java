package com.groudina.ten.demo.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class RoundTeamResultCancellationDto extends RoundTeamResultDto implements Serializable {
    private static final long serialVersionUID = -9167911416648916452L;

    private CancellationInfoDto cancellationInfo;

    public RoundTeamResultCancellationDto(int teamIdInGame, int roundNumber, double income, CancellationInfoDto cancellationInfo) {
        super(teamIdInGame, roundNumber, income);
        this.cancellationInfo = cancellationInfo;
    }
}
