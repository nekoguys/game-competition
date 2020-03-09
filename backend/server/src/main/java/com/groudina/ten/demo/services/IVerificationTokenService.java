package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.models.DbVerificationToken;
import reactor.core.publisher.Mono;

public interface IVerificationTokenService {
    Mono<DbVerificationToken> createVerificationToken(DbUser user);

    Mono<DbUser> verifyUser(String token);
}
