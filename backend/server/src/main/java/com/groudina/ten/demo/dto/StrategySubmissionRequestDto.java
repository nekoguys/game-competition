package com.groudina.ten.demo.dto;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StrategySubmissionRequestDto implements Serializable {
    private static final long serialVersionUID = -7784791780098442215L;

    @NonNull
    private String strategy;
}
