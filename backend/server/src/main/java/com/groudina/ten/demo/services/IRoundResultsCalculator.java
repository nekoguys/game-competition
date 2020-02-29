package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import com.groudina.ten.demo.models.DbRoundResultElement;

import java.util.List;

public interface IRoundResultsCalculator {
    List<DbRoundResultElement> calculateResults(DbCompetitionRoundInfo roundInfo, DbCompetition competition);
}
