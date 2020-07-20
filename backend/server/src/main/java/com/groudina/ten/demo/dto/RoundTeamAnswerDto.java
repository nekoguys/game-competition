package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RoundTeamAnswerDto.class, name = "regular"),
        @JsonSubTypes.Type(value = RoundTeamAnswerCancellationDto.class, name = "cancel")
})
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
