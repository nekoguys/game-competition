package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TeamsSettingCompetitionInfoResponse implements Serializable {
    @JsonProperty("showTeamMembers")
    private Boolean showTeamMembers;
}
