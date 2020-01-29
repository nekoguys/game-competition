package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TeamCreationEventDto implements Serializable {
    private static final long serialVersionUID = -4502650950386932982L;

    private String teamName;

    private int idInGame;

    private List<String> teamMembers;
}
