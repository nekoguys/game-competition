package com.groudina.ten.demo.services;

import reactor.core.publisher.Mono;

public interface IEmailService {
    Mono<Void> sendEmail(String email, String link);

    boolean isActive();
}
