package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewRoundLengthEventDto implements ITypedEvent {

    private static final long serialVersionUID = 8128268355654800986L;
    @Builder.Default
    private String type = "AddLength";

    private int additionalRoundLength;

    private int roundNumber;
}
