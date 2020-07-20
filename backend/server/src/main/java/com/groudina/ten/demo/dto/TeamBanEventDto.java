package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TeamBanEventDto.class, name = "regular"),
        @JsonSubTypes.Type(value = TeamBanEventCancellationDto.class, name = "cancel")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TeamBanEventDto {
    private int teamIdInGame;
    private String teamName;
    private int round;
}
