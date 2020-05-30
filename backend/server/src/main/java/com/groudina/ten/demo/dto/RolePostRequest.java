package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class RolePostRequest implements Serializable {
    @JsonProperty("role")
    private String role;

    private static final long serialVersionUID = -88005553535L;
}
