package com.groudina.ten.demo.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class TeamBanEventCancellationDto extends TeamBanEventDto implements Serializable {
    private static final long serialVersionUID = -3504827798268338863L;

    private CancellationInfoDto cancellationInfo;

    public TeamBanEventCancellationDto(int teamIdInGame, String teamName, int round, CancellationInfoDto cancellationInfo) {
        super(teamIdInGame, teamName, round);
        this.cancellationInfo = cancellationInfo;
    }
}
