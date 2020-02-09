package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndRoundEventDto implements ITypedEvent {
    private static final long serialVersionUID = 6160337879050418193L;

    @Builder.Default
    private String type = "EndRound";

    private boolean isEndOfGame;
}
