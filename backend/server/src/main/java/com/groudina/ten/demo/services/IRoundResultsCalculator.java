package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import com.groudina.ten.demo.models.DbRoundResultElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public interface IRoundResultsCalculator {

    @AllArgsConstructor
    @Getter
    @Builder
    static class RoundResultsHolder {
        private List<DbRoundResultElement> results;
        private double price;
    }

    RoundResultsHolder calculateResults(DbCompetitionRoundInfo roundInfo, DbCompetition competition);
}
