package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GamePinCheckResponse implements Serializable {
    private static final long serialVersionUID = -7231365746967677454L;

    @JsonProperty("exists")
    private boolean exists;

    public static GamePinCheckResponse of(boolean exists) {
        return new GamePinCheckResponse(exists);
    }
}
