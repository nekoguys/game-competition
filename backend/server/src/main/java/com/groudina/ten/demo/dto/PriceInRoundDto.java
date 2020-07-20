package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PriceInRoundDto.class, name = "regular"),
        @JsonSubTypes.Type(value = PriceInRoundCancellationDto.class, name = "cancel")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceInRoundDto implements Serializable {
    private static final long serialVersionUID = -1542413526670067000L;

    private double price;
    private int roundNumber;
}
