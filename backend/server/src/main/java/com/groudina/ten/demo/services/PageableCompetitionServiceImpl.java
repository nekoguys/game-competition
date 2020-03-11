package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.CompetitionInfoResponse;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Component
public class PageableCompetitionServiceImpl implements IPageableCompetitionService {
    private final DbCompetitionsRepository competitionsRepository;
    private final DbUserRepository userRepository;
    private final IEntitiesMapper<DbCompetition, CompetitionInfoResponse> competitionInfoResponseMapper;

    public PageableCompetitionServiceImpl(@Autowired DbCompetitionsRepository competitionsRepository,
                                          @Autowired DbUserRepository userRepository,
                                          @Autowired IEntitiesMapper<DbCompetition, CompetitionInfoResponse> competitionInfoResponseMapper) {
        this.competitionsRepository = competitionsRepository;
        this.userRepository = userRepository;
        this.competitionInfoResponseMapper = competitionInfoResponseMapper;
    }

    @Override
    public Mono<List<CompetitionInfoResponse>> get(String email, int startIndex, int amount) {
        return userRepository
                .findOneByEmail(email)
                .flatMapMany(competitionsRepository::findAllByOwner)
                .map(comp -> competitionInfoResponseMapper.map(comp, null))
                .sort(Comparator.nullsLast((a, b) -> -a.getLastUpdateTime().compareTo(b.getLastUpdateTime())))
                .skip(startIndex)
                .take(amount)
                .collectList();
    }
}
