package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RoundTeamResultDto implements Serializable {
    private static final long serialVersionUID = -6648537104241200589L;

    private int teamIdInGame;

    private int roundNumber;

    private double income;
}
