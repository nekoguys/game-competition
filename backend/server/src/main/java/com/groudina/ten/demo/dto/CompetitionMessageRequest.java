package com.groudina.ten.demo.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompetitionMessageRequest implements Serializable {
    private static final long serialVersionUID = 1574696649181677442L;

    private String message;
}
