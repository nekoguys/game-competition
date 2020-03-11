package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.UpdateProfileRequestDto;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbUserFromUpdateProfileUpdaterTest {
    DbUserFromUpdateProfileUpdater updater = null;

    PasswordEncoder passwordEncoder = null;

    @BeforeEach
    void setup() {
        passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return true;
            }
        };
        updater = new DbUserFromUpdateProfileUpdater(passwordEncoder);
    }

    @Test
    void update() {
        DbUser user = DbUser.builder()
                .email("email")
                .password("1234")
                .profile(
                        DbUser.Profile.builder()
                                .name("name")
                                .surname("surname")
                                .build())
                .build();
        UpdateProfileRequestDto requestDto = UpdateProfileRequestDto.builder().name("name1").newPassword("12345").surname("surname1").build();

        user = updater.update(user, requestDto).block();

        assertEquals(user.getProfile().getName(), "name1");
        assertEquals(user.getProfile().getSurname(), "surname1");
        assertEquals(user.getPassword(), "12345");

        user = updater.update(user, UpdateProfileRequestDto.builder().build()).block();

        assertEquals(user.getProfile().getName(), "name1");
        assertEquals(user.getProfile().getSurname(), "surname1");
        assertEquals(user.getPassword(), "12345");
    }
}