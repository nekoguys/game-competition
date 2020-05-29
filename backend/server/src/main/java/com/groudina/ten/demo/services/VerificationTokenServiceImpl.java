package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.datasource.DbVerificationTokenRepository;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.models.DbVerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class VerificationTokenServiceImpl implements IVerificationTokenService {

    private DbVerificationTokenRepository verificationTokenRepository;
    private DbUserRepository userRepository;
    private IEmailService emailService;

    @Value("${frontend.host.url}")
    private String host;

    public VerificationTokenServiceImpl(
            @Autowired DbVerificationTokenRepository tokenRepository,
            @Autowired DbUserRepository userRepository,
            @Autowired IEmailService emailService
    ) {
        this.verificationTokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostConstruct
    void test() {
        System.out.println("FRONTEND HOST: " + host);
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
    public Mono<DbUser> verifyUser(String token) {
        return verificationTokenRepository.findByToken(token).flatMap(el -> {
            var user = el.getUser();
            user.setVerified(true);

            return userRepository.save(user);
        });
    }

    private String getVerificationEndPoint(String token) {
        System.out.println("link: " + host + "/auth/verification/" + token);
        return host + "/auth/verification/" + token;
    }
}
