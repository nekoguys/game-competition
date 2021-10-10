package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.UserSearchRequest;
import com.groudina.ten.demo.dto.UserSearchResponse;
import com.groudina.ten.demo.services.IRolesMapper;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Collections;

@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(path = "/api/admin", produces = {MediaType.APPLICATION_JSON_VALUE})
@Controller
public class AdminController {
    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final DbUserRepository userRepository;
    private final IRolesMapper rolesMapper;

    public AdminController(@Autowired DbUserRepository userRepository,
                           @Autowired IRolesMapper rolesMapper) {
        this.userRepository = userRepository;
        this.rolesMapper = rolesMapper;
    }

    @PostMapping(path = "/search")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> search(@Valid @RequestBody UserSearchRequest request) {
        log.info("POST: /api/admin/search, body: {}", request);

        var query = request.getQuery().trim();
        if (!query.chars().allMatch(Character::isLetter)) {
            var errorBody = new ResponseMessage("Request must contain only letters");
            return Mono.just(ResponseEntity.badRequest().body(errorBody));
        }

        var pageRequest = PageRequest.of(request.getPage(), request.getPageSize());
        var regex = String.format("^%s", query);
        var results = userRepository.findByRegex(regex, pageRequest);

        return results.map(user -> {
            var role = rolesMapper.getTopRoleName(user.getRoles());
            var email = user.getEmail();
            return new UserSearchResponse.Info(email, role);
        }).collectList()
                .onErrorReturn(DataAccessException.class, Collections.emptyList())
                .map(infos -> (ResponseEntity) ResponseEntity.ok(new UserSearchResponse(infos)));
    }
}
