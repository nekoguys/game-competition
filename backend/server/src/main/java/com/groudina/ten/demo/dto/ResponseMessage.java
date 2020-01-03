package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ResponseMessage implements Serializable {
    @JsonProperty("message")
    private String message;

    private static final long serialVersionUID = -1764970284520387070L;
}
