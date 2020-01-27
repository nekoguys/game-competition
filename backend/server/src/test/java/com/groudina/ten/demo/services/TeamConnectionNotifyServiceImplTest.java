package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TeamConnectionNotifyServiceImplTest {

    private IEntitiesMapper<DbTeam, TeamCreationEventDto> mapper;

    @Mock
    private DbCompetitionsRepository repository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mapper = new DbTeamToTeamCreationEvent();
    }

    @Test
    void testSink() {
        var competition = DbCompetition.builder().pin("1234").state(DbCompetition.State.Registration).build();
        Mockito.when(repository.findByPin("1234")).thenReturn(Mono.just(competition));

        TeamConnectionNotifyServiceImpl connectionNotifyService = new TeamConnectionNotifyServiceImpl(mapper, repository);
        var team = DbTeam.builder().name("name").captain(DbUser.builder().email("email").build()).sourceCompetition(competition).build();
        competition.addTeam(team);

        connectionNotifyService.registerTeam(team);
        connectionNotifyService.getTeamEventForGame("1234").subscribe((val) -> {
            System.out.println(val.getTeamName());
        });

        StepVerifier verifier =
        StepVerifier
                .create(connectionNotifyService.getTeamEventForGame("1234"))
                .expectNextMatches(event -> { return event.getTeamName().equals("name"); })
                .expectNextCount(1)
                .thenCancel();

        connectionNotifyService.registerTeam(team);

        verifier.verify();
    }

    @Test
    void testSinkWithInitValues() {
        var competition = DbCompetition.builder().pin("1234").state(DbCompetition.State.Registration).build();
        var team = DbTeam.builder().name("name").captain(DbUser.builder().email("email").build()).sourceCompetition(competition).build();
        competition.addTeam(team);

        Mockito.when(repository.findByPin("1234")).thenReturn(Mono.just(competition));

        TeamConnectionNotifyServiceImpl connectionNotifyService = new TeamConnectionNotifyServiceImpl(mapper, repository);
        var team2 = DbTeam.builder().name("name1").captain(DbUser.builder().email("email2").build()).sourceCompetition(competition).build();

        competition.addTeam(team2);

        connectionNotifyService.registerTeam(team2);
        connectionNotifyService.getTeamEventForGame("1234").subscribe((val) -> {
            System.out.println(val.getTeamName());
        });

        StepVerifier verifier =
                StepVerifier
                        .create(connectionNotifyService.getTeamEventForGame("1234"))
                        .expectNextMatches(event -> { return event.getTeamName().equals("name"); })
                        .expectNextMatches(event -> event.getTeamName().equals("name1"))
                        .thenCancel();
        verifier.verify();

        //check replay
        StepVerifier
                .create(connectionNotifyService.getTeamEventForGame("1234"))
                .expectNextMatches(event -> { return event.getTeamName().equals("name"); })
                .expectNextMatches(event -> event.getTeamName().equals("name1"))
                .thenCancel().verify();
    }
}