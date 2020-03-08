package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.datasource.DbVerificationTokenRepository;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.models.DbVerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class VerificationTokenServiceImpl implements IVerificationTokenService {

    private DbVerificationTokenRepository verificationTokenRepository;
    private DbUserRepository userRepository;
    private IEmailService emailService;

    @Value("${host.url}")
    private static String host;

    public VerificationTokenServiceImpl(
            @Autowired DbVerificationTokenRepository tokenRepository,
            @Autowired DbUserRepository userRepository,
            @Autowired IEmailService emailService
    ) {
        this.verificationTokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public Mono<DbVerificationToken> createVerificationToken(DbUser user) {
        String token = UUID.randomUUID().toString();
        DbVerificationToken verificationToken = DbVerificationToken.builder()
                .token(token)
                .user(user)
                .build();
        return verificationTokenRepository.save(verificationToken).flatMap(savedToken -> {
            return this.emailService.sendEmail(user.getEmail(), getVerificationEndPoint(token)).then(Mono.just(savedToken));
        });
    }

    @Override
    public Mono<DbUser> verifyUser(DbVerificationToken token) {
        var user = token.getUser();
        user.setVerified(true);

        return userRepository.save(user);
    }

    private static String getVerificationEndPoint(String token) {
        return host + "/api/auth/verification/" + token;
    }
}
