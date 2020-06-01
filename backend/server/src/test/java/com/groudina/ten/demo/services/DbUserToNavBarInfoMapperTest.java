package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbUserToNavBarInfoMapperTest {
    DbUserToNavBarInfoMapper mapper = new DbUserToNavBarInfoMapper();
    @Test
    void map() {
        DbUser user = DbUser.builder()
                .profile(DbUser.Profile.builder().surname("surname").name("name").build())
                .roles(List.of(DbRole.builder().build()))
                .build();
        var res = mapper.map(user, null);
        assertEquals(res.getRole(), "Студент");
        assertEquals(res.getUserDescription(), "surname n.");
    }

    @Test
    void mapEmptyProfile() {
        DbUser user = DbUser.builder()
                .email("email")
                .roles(List.of(DbRole.builder().build()))
                .build();
        var res = mapper.map(user, null);
        assertEquals(res.getRole(), "Студент");
        assertEquals(res.getUserDescription(), "email");
    }
}