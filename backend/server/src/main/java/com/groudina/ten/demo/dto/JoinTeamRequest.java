package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JoinTeamRequest implements Serializable {
    private static final long serialVersionUID = -5660357487053059461L;

    private String competitionPin;

    private String teamName;

    private String password;

}
