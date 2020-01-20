package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groudina.ten.demo.models.DbRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RolesResponse implements Serializable {
    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("email")
    private String email;

    private static final long serialVersionUID = -1764970284520387071L;
}
