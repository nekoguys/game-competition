package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.CompetitionInfoResponse;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class PageableCompetitionServiceImpl implements IPageableCompetitionService {
    private final DbCompetitionsRepository competitionsRepository;
    private final DbUserRepository userRepository;
    private final DbTeamsRepository teamRepository;
    private final IEntitiesMapper<DbCompetition, CompetitionInfoResponse> competitionInfoResponseMapper;

    public PageableCompetitionServiceImpl(@Autowired DbCompetitionsRepository competitionsRepository,
                                          @Autowired DbUserRepository userRepository,
                                          @Autowired DbTeamsRepository teamRepository,
                                          @Autowired IEntitiesMapper<DbCompetition, CompetitionInfoResponse> competitionInfoResponseMapper) {
        this.competitionsRepository = competitionsRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.competitionInfoResponseMapper = competitionInfoResponseMapper;
    }

    @Override
    public Flux<CompetitionInfoResponse> getByOwner(String email, int startIndex, int amount) {
        return userRepository
                .findOneByEmail(email)
                .flatMapMany(competitionsRepository::findAllByOwner)
                .map(comp -> {
                    var res = competitionInfoResponseMapper.map(comp, null);
                    res.setOwned(true);
                    return res;
                })
                .skip(startIndex)
                .take(amount);
    }

    @Override
    public Flux<CompetitionInfoResponse> getByPlayer(String email, int startIndex, int amount) {
        return userRepository
                .findOneByEmail(email)
                .flatMapMany(user -> teamRepository.findDbTeamsByCaptainOrAllPlayers(user, user))
                .map(team -> competitionInfoResponseMapper.map(team.getSourceCompetition(), null))
                .skip(startIndex)
                .take(amount);
    }

    @Override
    public Flux<CompetitionInfoResponse> getByEmail(String email, Integer startIndex, Integer amount) {
        return getByOwner(email, 0, Integer.MAX_VALUE)
                .concatWith(getByPlayer(email, 0, Integer.MAX_VALUE))
                .distinct()
                .skip(startIndex)
                .take(amount);
    }
}
