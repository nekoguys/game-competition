package com.groudina.ten.demo.services;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PinGeneratorTest {

    @SneakyThrows
    @Test
    void generate() {
        String pin = (new PinGenerator()).generate();
        for (int i = 0; i < pin.length(); ++i)
            assertTrue(Character.isDigit(pin.charAt(i)));
        assertNotEquals("0", pin);
        Thread.sleep(0);
        String pin2 = (new PinGenerator()).generate();
        assertNotEquals(pin, pin2);
    }
}