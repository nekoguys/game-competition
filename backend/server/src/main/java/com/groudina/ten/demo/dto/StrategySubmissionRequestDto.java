package com.groudina.ten.demo.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class StrategySubmissionRequestDto implements Serializable {
    private static final long serialVersionUID = -7784791780098442215L;

    @NonNull
    private String strategy;
}
