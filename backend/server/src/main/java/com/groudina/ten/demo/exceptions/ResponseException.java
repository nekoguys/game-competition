package com.groudina.ten.demo.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseException extends RuntimeException {
    private String message;
}
