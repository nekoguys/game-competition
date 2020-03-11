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
public class ProfileInfoResponseDto implements Serializable {
    private static final long serialVersionUID = -8809039825965824993L;

    private String name;

    private String surname;

    private String email;
}
