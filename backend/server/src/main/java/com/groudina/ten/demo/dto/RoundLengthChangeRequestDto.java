package com.groudina.ten.demo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoundLengthChangeRequestDto implements Serializable {
    private static final long serialVersionUID = 3947703423473667158L;

    private int newRoundLength;
}
