package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements Serializable {
    @JsonProperty("email")
    @NonNull
    private String email;

    @JsonProperty("password")
    @NonNull
    private String password;

    private static final long serialVersionUID = -1764970284520387975L;
}
