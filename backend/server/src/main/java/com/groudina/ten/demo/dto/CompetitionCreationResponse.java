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
public class CompetitionCreationResponse implements Serializable {
    private static final long serialVersionUID = -3297956444527785556L;

    private String pin;
}
