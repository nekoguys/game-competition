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
public class NavBarInfoResponse implements Serializable {
    private static final long serialVersionUID = 7816504601865667701L;

    private String userDescription;

    private String role;
}
