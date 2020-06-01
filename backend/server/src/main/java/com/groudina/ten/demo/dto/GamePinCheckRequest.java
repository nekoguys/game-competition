package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GamePinCheckRequest implements Serializable {

    private static final long serialVersionUID = 8203174425825049599L;

    @JsonProperty("pin")
    private String pin;
}
