package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CancellationInfoDto implements Serializable {
    private static final long serialVersionUID = 7562144385803683974L;

    private int cancelRoundCount;
}
