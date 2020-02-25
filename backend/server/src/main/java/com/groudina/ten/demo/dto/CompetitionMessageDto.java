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
public class CompetitionMessageDto implements Serializable {
    private static final long serialVersionUID = -5054813915813060258L;

    private String message;
    private long sendTime;
}
