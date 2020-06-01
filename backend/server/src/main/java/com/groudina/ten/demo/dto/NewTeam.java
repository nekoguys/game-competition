package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class NewTeam implements Serializable {
    private static final long serialVersionUID = 909136386277440685L;

    @JsonProperty("game_id")
    private String competitionId;

    @JsonProperty("team_name")
    private String name;

    @JsonProperty("captain_email")
    private String captainEmail;

    private String password;
}
