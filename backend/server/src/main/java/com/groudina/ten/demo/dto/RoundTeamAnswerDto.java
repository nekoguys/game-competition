package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoundTeamAnswerDto implements Serializable {
    private static final long serialVersionUID = 3774481592076528731L;

    private int teamIdInGame;

    private int roundNumber;

    private int teamAnswer;
}
