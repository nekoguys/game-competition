package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse implements Serializable {
    private static final long serialVersionUID = -1235235235231L;

    private List<Info> results;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Info {
        private String email;
        private String role;
    }
}
