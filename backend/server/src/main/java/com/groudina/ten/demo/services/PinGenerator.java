package com.groudina.ten.demo.services;

import org.springframework.stereotype.Component;

@Component
public class PinGenerator implements IPinGenerator {
    @Override
    public String generate() {

        return String.valueOf(System.currentTimeMillis() - 1578000000000L);

    }
}
