package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.NavBarInfoResponse;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
public class DbUserToNavBarInfoMapper implements IEntitiesMapper<DbUser, NavBarInfoResponse> {
    @Override
    public NavBarInfoResponse map(DbUser user, Iterable<Pair<String, ?>> additionalFields) {
        String role = "Студент";
        if (user.getRoles().size() == 2) {
            role = "Учитель";
        } else if (user.getRoles().size() == 3) {
            role = "Админ";
        }

        String userDesc = user.getProfile().getSurname() + " " + user.getProfile().getName().substring(0, 1) + ".";

        return NavBarInfoResponse.builder()
                .role(role)
                .userDescription(userDesc)
                .build();
    }
}
