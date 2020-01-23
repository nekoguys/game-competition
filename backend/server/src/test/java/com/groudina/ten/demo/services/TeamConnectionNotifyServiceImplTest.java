package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class TeamConnectionNotifyServiceImplTest {

    private IEntitiesMapper<DbTeam, TeamCreationEventDto> mapper;

    @BeforeEach
    public void setup() {
        mapper = new DbTeamToTeamCreationEvent();
    }

    @Test
    void testSink() {
        TeamConnectionNotifyServiceImpl connectionNotifyService = new TeamConnectionNotifyServiceImpl(mapper);
        var competition = DbCompetition.builder().pin("1234").state(DbCompetition.State.Registration).build();
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
}