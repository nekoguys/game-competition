package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewRoundEventDto implements ITypedEvent {

    private static final long serialVersionUID = 2297590174396281095L;

    @Builder.Default
    private String type = "NewRound";

    private int roundLength;

    private long beginTime;

    private int roundNumber;
}
