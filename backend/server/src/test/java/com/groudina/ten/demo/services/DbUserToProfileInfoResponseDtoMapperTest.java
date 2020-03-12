package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbUserToProfileInfoResponseDtoMapperTest {
    DbUserToProfileInfoResponseDtoMapper mapper = new DbUserToProfileInfoResponseDtoMapper();
    @Test
    void map() {
        DbUser user = DbUser.builder().email("email").profile(DbUser.Profile.builder().name("name").surname("surname").build()).build();

        var res = mapper.map(user, null);
        assertEquals(res.getEmail(), "email");
        assertEquals(res.getName(), "name");
        assertEquals(res.getSurname(), "surname");
    }
}