package com.groudina.ten.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@ToString
public class ChangeUserPasswordRequestDto implements Serializable {
    private String userEmail;

    @JsonProperty("newPassword")
    private String newPassword = "1234";

    @JsonSetter("newPassword")
    public void setNewPassword(String s) {
        if (s != null) {
            newPassword = s;
        }
    }
}
