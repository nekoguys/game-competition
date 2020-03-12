package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionInfoResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPageableCompetitionService {
    Mono<List<CompetitionInfoResponse>> get(String email, int startIndex, int amount);
}
