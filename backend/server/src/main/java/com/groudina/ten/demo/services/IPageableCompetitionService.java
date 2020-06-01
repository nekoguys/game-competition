package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionInfoResponse;
import reactor.core.publisher.Flux;

public interface IPageableCompetitionService {
    Flux<CompetitionInfoResponse> getByOwner(String email, int startIndex, int amount);
    Flux<CompetitionInfoResponse> getByPlayer(String email, int startIndex, int amount);
    Flux<CompetitionInfoResponse> getByEmail(String email, Integer startIndex, Integer amount);
}
