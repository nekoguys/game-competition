package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto implements Serializable {
    private static final long serialVersionUID = 4034243336906534261L;

    private String name;

    private String surname;

    private String newPassword;
}
