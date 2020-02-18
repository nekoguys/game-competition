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
public class CompetitionMessageRequest implements Serializable {
    private static final long serialVersionUID = 1574696649181677442L;

    private String message;
}
