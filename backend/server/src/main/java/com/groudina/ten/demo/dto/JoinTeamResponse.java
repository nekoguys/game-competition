package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinTeamResponse implements Serializable {

    private static final long serialVersionUID = -5844086227560779192L;

    private String currentTeamName;
}
