package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.ProfileInfoResponseDto;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DbUserToProfileInfoResponseDtoMapper implements IEntitiesMapper<DbUser, ProfileInfoResponseDto> {
    @Override
    public ProfileInfoResponseDto map(DbUser user, Iterable<Pair<String, ?>> additionalFields) {
        var name = Objects.isNull(user.getProfile()) ? "": user.getProfile().getName();
        var surname = Objects.isNull(user.getProfile()) ? "": user.getProfile().getSurname();
        return ProfileInfoResponseDto.builder()
                .email(user.getEmail())
                .name(name)
                .surname(surname)
                .build();
    }
}
