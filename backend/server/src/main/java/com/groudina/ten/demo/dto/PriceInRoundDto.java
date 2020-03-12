package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceInRoundDto implements Serializable {
    private static final long serialVersionUID = -1542413526670067000L;

    private double price;
    private int roundNumber;
}
