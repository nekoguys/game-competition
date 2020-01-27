package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IAddTeamToCompetitionService;
import com.groudina.ten.demo.services.ITeamConnectionNotifyService;
import com.groudina.ten.demo.services.ITeamCreationChecker;
import com.groudina.ten.demo.services.ITeamJoinService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
@EnableEmbeddedMongo
@Log4j2
class CompetitionsControllerTest {

    @Autowired
    ApplicationContext context;

    @Configuration
    class MongoConfig {}

    WebTestClient webTestClient;

    @Autowired
    CompetitionsController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @Autowired
    DbTeamsRepository teamsRepository;

    @Autowired
    IAddTeamToCompetitionService addTeamToCompetitionService;

    @Autowired
    ITeamCreationChecker checker;

    @Autowired
    ITeamConnectionNotifyService notifyService;

    @Autowired
    ITeamJoinService teamJoinService;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(this.context).apply(springSecurity()).configureClient().build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
        competitionsRepository.deleteAll().block();
        teamsRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void createCompetitionForbidden() {
        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("draft")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void createCompetition() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collect(Collectors.toList()).block()).build()).block();
        assertEquals(0, competitionsRepository.findAll().count().block());

        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("draft")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class)
        ;
        assertEquals(1, competitionsRepository.findAll().count().block());
        assertEquals(competitionsRepository.findAll().collect(Collectors.toList()).block().get(0).getOwner(), owner);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void checkPinCompetition() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collect(Collectors.toList()).block()).build()).block();
        assertEquals(0, competitionsRepository.findAll().count().block());

        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("draft")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class)
        ;
        assertEquals(competitionsRepository.findAll().collect(Collectors.toList()).block().get(0).getPin(), null);

        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("registration")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class)
        ;

        var t = competitionsRepository
                .findAll()
                .toStream()
                .filter(x -> x.getPin() != null)
                .collect(Collectors.toList())
                .get(0);
        assertNotEquals(t.getPin(), null);
        assertEquals(competitionsRepository.findByPin(t.getPin()).block().getPin(), t.getPin());
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamCreation() {
        userRepository.save(DbUser.builder()
                .email("email")
                .roles(List.of(rolesRepository.findByName("ROLE_STUDENT").block()))
                .build()).block();
        competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("123").build()).block();

        webTestClient.post().uri("/api/competitions/create_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        NewTeam.builder()
                                .captainEmail("email")
                                .competitionId("123")
                                .password("password")
                                .build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class);

        var dbComp = competitionsRepository.findAll().collect(Collectors.toList()).block().get(0);
        assertEquals(dbComp.getTeams().size(), 1);
        assertEquals(dbComp.getTeams().get(0).getCaptain().getEmail(), "email");
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamCreationStateFail() {
        var user = userRepository.save(DbUser.builder()
                .email("email")
                .roles(List.of(rolesRepository.findByName("ROLE_STUDENT").block()))
                .build()).block();


        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Draft)
                .pin("123").build()).block();

        DbTeam dbTeam = teamsRepository.save(DbTeam.builder().captain(user).sourceCompetition(competition).build()).block();

        webTestClient.post().uri("/api/competitions/create_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        NewTeam.builder()
                                .captainEmail("email")
                                .competitionId("123")
                                .password("password")
                                .name("name")
                                .build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).isEqualTo(ResponseMessage.of("Illegal game state"));
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamCreationUserFail() {
        var user = userRepository.save(DbUser.builder()
                .email("email")
                .roles(List.of(rolesRepository.findByName("ROLE_STUDENT").block()))
                .build()).block();


        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("123").build()).block();

        DbTeam dbTeam = teamsRepository.save(DbTeam.builder().name("oldname").captain(user).sourceCompetition(competition).build()).block();

        competition.addTeam(dbTeam);
        competitionsRepository.save(competition).block();

        webTestClient.post().uri("/api/competitions/create_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        NewTeam.builder()
                                .name("teamname")
                                .captainEmail("email")
                                .competitionId("123")
                                .password("password")
                                .build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).isEqualTo(ResponseMessage.of("Captain is in another team already"));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    public void testCheckGamePin() {
        var competition = competitionsRepository.save(DbCompetition.builder().pin("123")
                .state(DbCompetition.State.Registration).build()).block();
        var competition2 = competitionsRepository.save(DbCompetition.builder().pin("1234")
                .state(DbCompetition.State.Draft).build()).block();

        webTestClient.post().uri("/api/competitions/check_pin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        GamePinCheckRequest.builder().pin("123").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(GamePinCheckResponse.class).consumeWith((resp) -> {
            assertTrue(resp.getResponseBody().isExists());
        });

        webTestClient.post().uri("/api/competitions/check_pin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(GamePinCheckRequest.builder().pin("12").build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(GamePinCheckResponse.class).consumeWith((resp) -> {
            assertFalse(resp.getResponseBody().isExists());
        });

        webTestClient.post().uri("/api/competitions/check_pin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(GamePinCheckRequest.builder().pin("1234").build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(GamePinCheckResponse.class).consumeWith((resp) -> {
            assertFalse(resp.getResponseBody().isExists());
        });
    }

    private List<Object> testTeamJoinCommon() {
        var user = userRepository.save(DbUser.builder()
                .email("email")
                .password("1234")
                .roles(rolesRepository.findAll().collect(Collectors.toList()).block())
                .build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("12345")
                .build()).block();

        var team = teamsRepository.save(DbTeam.builder()
                .sourceCompetition(competition)
                .name("TEAMNAME")
                .password("password")
                .idInGame(0)
                .build()).block();

        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        return List.of(user, competition, team);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoin() {
        testTeamJoinCommon();


        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("password")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(JoinTeamResponse.class).value((resp) -> {
                    assertEquals(resp.getCurrentTeamName(), "TEAMNAME");
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinNoSuchTeam() {
        testTeamJoinCommon();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("password")
                                .teamName("NONE").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(resp -> {
                    System.out.println(resp.getMessage());
                    assertTrue(resp.getMessage().contains("No team in competition with name"));
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinNoSuchCompetition() {
        testTeamJoinCommon();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("NONE")
                                .password("password")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(resp -> {
                    System.out.println(resp.getMessage());
                    assertTrue(resp.getMessage().contains("No competition with pin"));
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinWrongPassword() {
        testTeamJoinCommon();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("wrongpassword")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(resp -> {
                    System.out.println(resp.getMessage());
                    assertEquals("Wrong team password", resp.getMessage());
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinUserInAnotherTeam() {
        var lst = testTeamJoinCommon();
        DbUser user = (DbUser)lst.get(0);
        DbCompetition competition = (DbCompetition)lst.get(1);

        var anotherTeam = teamsRepository.save(DbTeam
                .builder()
                .sourceCompetition(competition)
                .name("ANOTHERNAME")
                .password("pass")
                .allPlayers(List.of(user))
                .build()).block();
        competition.addTeam(anotherTeam);

        competitionsRepository.save(competition).block();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("password")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value((resp) -> {
                    System.out.println(resp.getMessage());
                    assertTrue(resp.getMessage().contains("another team"));
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinAlreadyInThisTeam() {
        var lst = testTeamJoinCommon();
        DbUser user = (DbUser)lst.get(0);
        DbTeam team = (DbTeam)lst.get(2);

        team.addPlayer(user);
        teamsRepository.save(team).block();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("password")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(resp -> {
            System.out.println(resp.getMessage());
            assertTrue(resp.getMessage().contains("another team"));
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinWrongGameState() {
        var lst = testTeamJoinCommon();
        DbCompetition competition = (DbCompetition)lst.get(1);
        competition.setState(DbCompetition.State.InProcess);

        competitionsRepository.save(competition).block();

        webTestClient.post().uri("/api/competitions/join_team")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        JoinTeamRequest
                                .builder()
                                .competitionPin("12345")
                                .password("password")
                                .teamName("TEAMNAME").build()
                )).accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value((resp) -> {
                    System.out.println(resp.getMessage());
                    assertEquals("Illegal competition state", resp.getMessage());
        });
    }
}
