package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.dto.NewTeam;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.exceptions.CaptainAlreadyCreatedGameException;
import com.groudina.ten.demo.exceptions.IllegalGameStateException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.services.IAddTeamToCompetitionService;
import com.groudina.ten.demo.services.IEntitiesMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Log4j2
@RequestMapping(path="/api/competitions", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@Controller
public class CompetitionsController {
    private DbCompetitionsRepository competitionsRepository;
    private DbUserRepository userRepository;
    private IEntitiesMapper<NewCompetition, DbCompetition> competitionMapper;
    private IAddTeamToCompetitionService teamJoinService;

    public CompetitionsController(@Autowired DbCompetitionsRepository repository,
                                  @Autowired DbUserRepository userRepository,
                                  @Autowired IEntitiesMapper<NewCompetition, DbCompetition> mapper,
                                  @Autowired IAddTeamToCompetitionService teamJoinService) {
        this.competitionsRepository = repository;
        this.userRepository = userRepository;
        this.competitionMapper = mapper;
        this.teamJoinService = teamJoinService;
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> createCompetition(Mono<Principal> principalMono, @Valid @RequestBody NewCompetition competition) {
        return principalMono.map(principal -> {
            log.error(principal.getName());
            return principal.getName();
        }).flatMap(userEmail -> {
            return userRepository.findOneByEmail(userEmail);
        }).flatMap(dbUser -> {
            var dbCompetition = competitionMapper.map(competition, List.of(Pair.of("owner", dbUser)));
            return competitionsRepository.save(dbCompetition);
        }).map(newCompetition -> {
            return ResponseEntity.ok(ResponseMessage.of("Competition Created Successfully"));
        });
    }

    @PostMapping(value = "/create_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<ResponseMessage>> joinTeam(@Valid @RequestBody NewTeam newTeam) {
        return this.teamJoinService.addTeamToCompetition(newTeam).map(team -> {
            return ResponseEntity.ok(ResponseMessage.of("Team created successfully"));
        })
                .onErrorReturn(CaptainAlreadyCreatedGameException.class,
                    new ResponseEntity<>(ResponseMessage.of("Captain is in another team already"), HttpStatus.BAD_REQUEST))
                .onErrorReturn(IllegalGameStateException.class,
                        new ResponseEntity<>(ResponseMessage.of("Illegal game state"), HttpStatus.BAD_REQUEST));
    }
}



