package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewTeam;
import com.groudina.ten.demo.exceptions.CaptainAlreadyCreatedGameException;
import com.groudina.ten.demo.exceptions.IllegalGameStateException;
import com.groudina.ten.demo.exceptions.WrongCompetitionParametersException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddTeamToCompetitionServiceImpl implements IAddTeamToCompetitionService {
    private DbUserRepository userRepository;

    private DbCompetitionsRepository competitionsRepository;

    private DbTeamsRepository teamsRepository;

    private ITeamCreationChecker creationChecker;

    private ITeamConnectionNotifyService connectionNotifyService;

    public AddTeamToCompetitionServiceImpl(@Autowired DbUserRepository userRepository,
                                           @Autowired DbCompetitionsRepository competitionsRepository,
                                           @Autowired DbTeamsRepository teamsRepository,
                                           @Autowired ITeamCreationChecker creationChecker,
                                           @Autowired ITeamConnectionNotifyService connectionNotifyService) {
        this.userRepository = userRepository;
        this.competitionsRepository = competitionsRepository;
        this.teamsRepository = teamsRepository;
        this.creationChecker = creationChecker;
        this.connectionNotifyService = connectionNotifyService;
    }

    @Override
    public Mono<DbTeam> addTeamToCompetition(NewTeam newTeam) {
        var zipped = Mono.zip(userRepository.findOneByEmail(newTeam.getCaptainEmail()),
                competitionsRepository.findByPin(newTeam.getCompetitionId()));
        return zipped.
                flatMap((captainAndCompTuple) -> {
                    var captain = captainAndCompTuple.getT1();
                    var competition = captainAndCompTuple.getT2();

                    if (competition.getState() != DbCompetition.State.Registration) {
                        return Mono.error(new IllegalGameStateException("This game is in state " +
                                competition.getState().name() + " but not in registration state"));
                    }

                    if (!creationChecker.checkCreation(competition, captain)) {
                        return Mono.error(new CaptainAlreadyCreatedGameException(newTeam.getCaptainEmail() +
                                " is already in another team"));
                    }

                    var dbTeam = DbTeam.builder()
                            .password(newTeam.getPassword())
                            .captain(captain)
                            .sourceCompetition(competition)
                            .name(newTeam.getName())
                            //.idInGame() TODO
                            .build();
                    competition.addTeam(dbTeam);
                    return teamsRepository.save(dbTeam).flatMap(team -> {
                        this.connectionNotifyService.registerTeam(team);
                        return competitionsRepository.save(competition);
                    }).then(Mono.just(dbTeam)).switchIfEmpty(Mono.error(new WrongCompetitionParametersException("Game or captain not found")));
        });
    }
}
