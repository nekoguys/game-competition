package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IEntitiesMapper;
import com.groudina.ten.demo.services.IPinGenerator;
import com.groudina.ten.demo.services.NewCompetitionToDbMapper;
import com.groudina.ten.demo.services.PinGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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
import java.io.Console;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequestMapping(path="/api/competitions", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@Controller
public class CompetitionsController {
    private DbCompetitionsRepository competitionsRepository;
    private DbUserRepository userRepository;
    private IEntitiesMapper<NewCompetition, DbCompetition> competitionMapper;
    private IPinGenerator pinGenerator;

    public CompetitionsController(@Autowired DbCompetitionsRepository repository,
                                  @Autowired DbUserRepository userRepository,
                                  @Autowired IEntitiesMapper<NewCompetition, DbCompetition> mapper,
                                  @Autowired IPinGenerator pinGenerator) {
        this.competitionsRepository = repository;
        this.userRepository = userRepository;
        this.competitionMapper = mapper;
        this.pinGenerator = pinGenerator;
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
            ArrayList<Pair<String, ?>> params = new ArrayList<Pair<String, ?>>();
            params.add(Pair.of("owner", dbUser));
            System.out.println(competition.getState());
            if (competition.getState().equals(DbCompetition.State.Registration.toString().toLowerCase()))
                params.add(Pair.of("pin", pinGenerator.generate()));
            var dbCompetition = competitionMapper.map(competition, params);
            return competitionsRepository.save(dbCompetition);
        }).map(newCompetition -> {
            return ResponseEntity.ok(ResponseMessage.of("Competition Created Successfully"));
        });
    }
}
