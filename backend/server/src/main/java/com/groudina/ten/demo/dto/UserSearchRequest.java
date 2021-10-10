package com.groudina.ten.demo.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserSearchRequest implements Serializable {
    private static final long serialVersionUID = 2440487784045418392L;

    private String query;

    private int page;

    private int pageSize;
}
