package com.groudina.ten.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class PriceInRoundCancellationDto extends PriceInRoundDto implements Serializable {
    private static final long serialVersionUID = -4754016461486739492L;

    private CancellationInfoDto cancellationInfo;

    public PriceInRoundCancellationDto(double price, int roundNumber, CancellationInfoDto cancellationInfo) {
        super(price, roundNumber);
        this.cancellationInfo = cancellationInfo;
    }
}
