package com.groudina.ten.demo.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class CompetitionAnswerRequestDto implements Serializable {

    private static final long serialVersionUID = -2068405266676142856L;

    private int answer;

    private int roundNumber;
}
