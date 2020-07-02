package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CompetitionRoundLengthChangerImpl implements ICompetitionRoundLengthChanger {
    private DbCompetitionsRepository competitionsRepository;

    public CompetitionRoundLengthChangerImpl(
            @Autowired DbCompetitionsRepository competitionsRepository
    ) {
        this.competitionsRepository = competitionsRepository;
    }

    @Override
    public Mono<DbCompetition> changeRoundLength(DbCompetition competition, int newRoundLength) {
        var history = competition.getParameters().getRoundsLengthHistory();
        int currentRound = competition.getCompetitionProcessInfo().getCurrentRoundNumber();

        history.add(currentRound, newRoundLength);

        return competitionsRepository.save(competition);
    }
}
