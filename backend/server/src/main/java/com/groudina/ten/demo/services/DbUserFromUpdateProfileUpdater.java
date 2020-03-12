package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.UpdateProfileRequestDto;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class DbUserFromUpdateProfileUpdater implements IEntityUpdater<DbUser, UpdateProfileRequestDto> {
    private PasswordEncoder passwordEncoder;

    public DbUserFromUpdateProfileUpdater(@Autowired PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<DbUser> update(DbUser user, UpdateProfileRequestDto dto) {
        var profile = user.getProfile();

        if (Objects.isNull(profile)) {
            profile = new DbUser.Profile();
            user.setProfile(profile);
        }

        String name = strip(dto.getName());
        if (Objects.nonNull(name) && name.length() > 0) {
            profile.setName(name);
        }

        String surname = strip(dto.getSurname());
        if (Objects.nonNull(surname) && surname.length() > 0) {
            profile.setSurname(surname);
        }

        String password = strip(dto.getNewPassword());
        if (Objects.nonNull(password) && password.length() > 0) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return Mono.just(user);
    }

    private static String strip(@Nullable String str) {
        if (Objects.nonNull(str)) {
            return str.strip();
        }

        return null;
    }
}
