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
        @JsonSubTypes.Type(value = RoundTeamResultDto.class, name = "regular"),
        @JsonSubTypes.Type(value = RoundTeamResultCancellationDto.class, name = "cancel")
})
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
