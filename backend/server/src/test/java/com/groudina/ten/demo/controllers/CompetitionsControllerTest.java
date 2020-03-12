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
import com.groudina.ten.demo.services.*;
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

    @Autowired
    IGameManagementService gameManagementService;

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
        var params = NewCompetition.builder()
                .shouldShowStudentPreviousRoundResults(false)
                .state("draft")
                .maxTeamsAmount(1)
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .name("name")
                .roundLength(1)
                .roundsCount(2)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(true)
                .maxTeamSize(3)
                .build();
        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(params))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class)
        ;
        assertEquals(1, competitionsRepository.findAll().count().block());
        var competition = competitionsRepository.findAll().collectList().block().get(0);
        assertEquals(owner, competition.getOwner());
        assertEquals(params.getState(), competition.getState().name().toLowerCase());
        assertEquals(params.getShouldEndRoundBeforeAllAnswered(), competition.getParameters().isShouldEndRoundBeforeAllAnswered());
        assertEquals(params.getShouldShowStudentPreviousRoundResults(), competition.getParameters().isShouldShowStudentPreviousRoundResults());
        assertEquals(params.getShouldShowResultTableInEnd(), competition.getParameters().isShouldShowResultTableInEnd());
        assertEquals(params.getName(), competition.getParameters().getName());
        assertEquals(params.getRoundLength(), competition.getParameters().getRoundLengthInSeconds());
        assertEquals(params.getMaxTeamSize(), competition.getParameters().getMaxTeamSize());
        assertEquals(params.getMaxTeamsAmount(), competition.getParameters().getMaxTeamsAmount());
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void createCompetitionRegistration() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collect(Collectors.toList()).block()).build()).block();
        assertEquals(0, competitionsRepository.findAll().count().block());
        var params = NewCompetition.builder()
                .shouldShowStudentPreviousRoundResults(false)
                .state("registration")
                .maxTeamsAmount(1)
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .name("name")
                .roundLength(1)
                .roundsCount(2)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(true)
                .maxTeamSize(3)
                .build();
        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(params))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(CompetitionCreationResponse.class)
                .value((resp) -> {
                    assertNotNull(resp.getPin());
                })
        ;
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
        assertNotNull(competitionsRepository.findAll().collect(Collectors.toList()).block().get(0).getPin());

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
                .pin("123")
                .parameters(DbCompetition.Parameters
                        .builder()
                        .maxTeamSize(20)
                        .maxTeamsAmount(20)
                        .build()).build())
                .block();

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
                .pin("123")
                .parameters(DbCompetition.Parameters
                        .builder()
                        .maxTeamSize(20)
                        .maxTeamsAmount(20)
                        .build())
                .build()).block();

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
                .expectBody(ResponseMessage.class).value((resp) ->
                assertTrue(resp.getMessage().contains("Illegal game state")));
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
                .pin("123")
                .parameters(DbCompetition.Parameters
                        .builder()
                        .maxTeamSize(20)
                        .maxTeamsAmount(20)
                        .build())
                .build()).block();

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
                .expectBody(ResponseMessage.class).value(resp -> {
                    assertTrue(resp.getMessage().contains("is Captain and is in another team already"));
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamCreationTooMuchTeams() {
        var user = userRepository.save(DbUser.builder()
                .email("email")
                .roles(List.of(rolesRepository.findByName("ROLE_STUDENT").block()))
                .build()).block();


        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("123")
                .parameters(DbCompetition.Parameters
                        .builder()
                        .maxTeamSize(20)
                        .maxTeamsAmount(1)
                        .build())
                .build()).block();

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
                .expectBody(ResponseMessage.class).isEqualTo(ResponseMessage.of("There are too much teams in competition max amount: 1"));
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

    private List<Object> testTeamJoinCommon(int teamSize) {
        var user = userRepository.save(DbUser.builder()
                .email("email")
                .password("1234")
                .roles(rolesRepository.findAll().collect(Collectors.toList()).block())
                .build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("12345")
                .parameters(DbCompetition.Parameters
                        .builder()
                        .maxTeamSize(teamSize)
                        .build())
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
        testTeamJoinCommon(20);


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
        testTeamJoinCommon(20);

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
        testTeamJoinCommon(20);

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
        testTeamJoinCommon(20);

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
        var lst = testTeamJoinCommon(20);
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
        var lst = testTeamJoinCommon(20);
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
        var lst = testTeamJoinCommon(20);
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

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testTeamJoinTeamOverflow() {
        testTeamJoinCommon(0);

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
            assertEquals("There are too much team members already, max amount: 0", resp.getMessage());
        });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    public void testGetCompetitionInfo() {
        var user = DbUser.builder().email("email").password("1234").roles(rolesRepository.findAll().collectList().block()).build();
        user = userRepository.save(user).block();

        var params = DbCompetition.Parameters.builder()
                .maxTeamsAmount(1)
                .maxTeamSize(2)
                .name("name")
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .roundLengthInSeconds(60)
                .roundsCount(3)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(false)
                .shouldShowStudentPreviousRoundResults(true)
                .build();
        var comp = DbCompetition.builder().pin("12345").parameters(params).build();
        comp = competitionsRepository.save(comp).block();

        webTestClient.get().uri("/api/competitions/get_clone_info/12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.expenses_formula").isEqualTo("1;2;3")
                .jsonPath("$.demand_formula").isEqualTo("1;2")
                .jsonPath("$.should_show_student_previous_round_results").isEqualTo(true);//test some fields for correct json paths
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    public void testUpdateCompetition() {
        var user = userRepository.save(DbUser.builder().email("email").password("1234")
                .roles(rolesRepository.findAll().collectList().block()).build()).block();
        var params = DbCompetition.Parameters.builder()
                .maxTeamsAmount(1)
                .maxTeamSize(2)
                .name("name")
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .roundLengthInSeconds(60)
                .roundsCount(3)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(false)
                .shouldShowStudentPreviousRoundResults(true)
                .build();
        var comp = DbCompetition.builder().pin("12345").parameters(params).build();
        comp = competitionsRepository.save(comp).block();

        var newParams = NewCompetition.builder()
                .maxTeamsAmount(5)
                .maxTeamSize(6)
                .name("namename")
                .demandFormula(List.of("2", "3"))
                .expensesFormula(List.of("2", "3", "4"))
                .build();

        webTestClient.post().uri("/api/competitions/update_competition/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newParams))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(CompetitionCloneInfoResponse.class)
                .value((resp) -> {
                    assertEquals(resp.getMaxTeamsAmount(), 5);
                    assertEquals(resp.getMaxTeamSize(), 6);
                    assertEquals(resp.getName(), "namename");
                    assertEquals(resp.getDemandFormula(), "2;3");
                    assertEquals(resp.getExpensesFormula(), "2;3;4");
                });
        comp = competitionsRepository.findByPin("12345").block();

        assertEquals(comp.getParameters().getMaxTeamSize(), 6);
        assertEquals(comp.getParameters().getMaxTeamsAmount(), 5);
        assertEquals(comp.getParameters().getName(), "namename");
        assertIterableEquals(comp.getParameters().getDemandFormula(), List.of("2", "3"));
        assertIterableEquals(comp.getParameters().getExpensesFormula(), List.of("2", "3", "4"));
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    public void testGetCreatedCompetitions() {
        var user = DbUser.builder().email("email").password("1234").roles(rolesRepository.findAll().collectList().block()).build();
        user = userRepository.save(user).block();

        var params = DbCompetition.Parameters.builder()
                .maxTeamsAmount(1)
                .maxTeamSize(2)
                .name("name1")
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .roundLengthInSeconds(60)
                .roundsCount(3)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(false)
                .shouldShowStudentPreviousRoundResults(true)
                .build();
        var comp = DbCompetition.builder().pin("12345").owner(user).parameters(params).build();
        comp = competitionsRepository.save(comp).block();

        var params2 = DbCompetition.Parameters.builder()
                .maxTeamsAmount(2)
                .maxTeamSize(3)
                .name("name2")
                .demandFormula(List.of("3", "4"))
                .expensesFormula(List.of("4", "5", "6"))
                .instruction("instr2")
                .roundLengthInSeconds(120)
                .roundsCount(2)
                .shouldEndRoundBeforeAllAnswered(false)
                .shouldShowResultTableInEnd(true)
                .shouldShowStudentPreviousRoundResults(false)
                .build();
        var comp2 = DbCompetition.builder().pin("23456").owner(user).parameters(params).build();
        comp2 = competitionsRepository.save(comp2).block();

        webTestClient.get().uri("/api/competitions/competitions_history/0/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].length()").isEqualTo(15)
                .jsonPath("$[1].length()").isEqualTo(15);
    }
    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void testResultsFormatter() {
        var user = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var captain = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var captain2 = userRepository.save(DbUser.builder().password("1234").email("tutEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(2)
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain2).idInGame(0).name("abac").sourceCompetition(competition).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(1).name("kuks").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition.addTeam(team2);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();

        gameManagementService.submitAnswer(competition, team, 10, 1).block();
        gameManagementService.submitAnswer(competition, team2, 30, 1).block();

        gameManagementService.endCurrentRound(competition).block();

        gameManagementService.startNewRound(competition).block();

        gameManagementService.submitAnswer(competition, team, 20, 2).block();
        gameManagementService.submitAnswer(competition, team2, 40, 2).block();

        gameManagementService.endCurrentRound(competition).block();

        webTestClient.get().uri("/api/competitions/competition_results/1234")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ICompetitionResultsFormatter.CompetitionResults.class)
                .value(competitionResults -> {
                    assertIterableEquals(competitionResults.getTeams().get(0).getTeamMembers(), List.of("tutEmail"));
                    assertEquals(competitionResults.getIncome().get(1).get(0), -63, 0.01);
                    assertEquals(competitionResults.getIncome().get(1).get(1), -783, 0.01);

                    assertEquals(competitionResults.getIncome().get(2).get(0), -363, 0.01);
                    assertEquals(competitionResults.getIncome().get(2).get(1), -1523, 0.01);

                    assertIterableEquals(competitionResults.getTeams().get(1).getTeamMembers(), List.of("anotherEmail"));

                    assertIterableEquals(competitionResults.getTeamsOrderInDecreasingByTotalPrice(), List.of(0, 1));
                    assertEquals(competitionResults.getPrices().get(1), 6);
                    assertEquals(competitionResults.getPrices().get(2), 4);
                    assertEquals(competitionResults.getProduced().get(1).get(0), 10);
                    assertEquals(competitionResults.getProduced().get(1).get(1), 30);
                    assertEquals(competitionResults.getProduced().get(2).get(0), 20);
                    assertEquals(competitionResults.getProduced().get(2).get(1), 40);
                });
    }

}
