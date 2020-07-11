package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

public interface IStrategySubmissionService {
    public Mono<Void> submitStrategy(String submitterEmail, DbCompetition competition, StrategyHolder holder);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyHolder {
        private String strategy;
    }
}
