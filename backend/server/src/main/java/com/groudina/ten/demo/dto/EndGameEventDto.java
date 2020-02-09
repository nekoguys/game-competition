package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndGameEventDto implements ITypedEvent {
    private static final long serialVersionUID = -6074634725374653447L;

    @Builder.Default
    private String type = "EndGame";
}
