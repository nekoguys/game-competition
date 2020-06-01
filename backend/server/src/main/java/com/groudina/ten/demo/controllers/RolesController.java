package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.RolePostRequest;
import com.groudina.ten.demo.dto.RoleResponse;
import com.groudina.ten.demo.exceptions.ResponseException;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IRolesMapper;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(path = "/api/roles/{email}")
@Controller
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class RolesController {
    private final Logger log = LoggerFactory.getLogger(RolesController.class);
    private DbUserRepository userRepository;
    private DbRolesRepository rolesRepository;
    private IRolesMapper rolesMapper;

    public RolesController(
            @Autowired DbUserRepository userRepository,
            @Autowired DbRolesRepository rolesRepository,
            @Autowired IRolesMapper rolesMapper) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.rolesMapper = rolesMapper;
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity> postRoles(
            Mono<Principal> principalMono,
            @PathVariable String email,
            @Valid @RequestBody RolePostRequest rolePostRequest) {
        String roleName = rolePostRequest.getRole();

        Mono<DbUser> emailNotFoundFallback = Mono.error(
                new ResponseException(String.format("User with email \"%s\" doesn't exist", email)));
        Mono<DbUser> targetMono = userRepository.findOneByEmail(email)
                .switchIfEmpty(emailNotFoundFallback);

        Mono<List<DbRole>> targetRolesMono = rolesMapper.map(roleName);

        return Mono.zip(principalMono, targetMono, targetRolesMono).flatMap(args -> {
            Principal principal = args.getT1();
            DbUser target = args.getT2();
            List<DbRole> targetRoles = args.getT3();

            log.info("POST: /api/roles/{}, email: {}, body: {}", email, principal.getName(), rolePostRequest);

            // Check if admin commits suicide.
            if (email.equals(principal.getName()) && !roleName.equals("ROLE_ADMIN"))
                return Mono.error(new ResponseException("You are not allowed to remove your role \"ROLE_ADMIN\""));

            target.setRoles(targetRoles);
            ResponseEntity response = ResponseEntity.ok(new RoleResponse(roleName));
            return userRepository.save(target).thenReturn(response);
        }).onErrorResume(ex -> {
            return Mono.just(ResponseEntity.badRequest().body(
                    new ResponseMessage(ex.getMessage())));
        });
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity> getRoles(@PathVariable String email) {
        log.info("GET: /api/roles/{}", email);

        Mono<DbUser> emailNotFoundFallback = Mono.error(
                new ResponseException(String.format("User with email \"%s\" doesn't exist", email)));

        return userRepository.findOneByEmail(email)
                .switchIfEmpty(emailNotFoundFallback)
                .map(user -> {
                    var roleNames = user.getRoles().stream()
                            .map(DbRole::getName)
                            .collect(Collectors.toList());

                    if (roleNames.contains("ROLE_ADMIN"))
                        return "ROLE_ADMIN";
                    if (roleNames.contains("ROLE_TEACHER"))
                        return "ROLE_TEACHER";
                    if (roleNames.contains("ROLE_STUDENT"))
                        return "ROLE_STUDENT";

                    throw new ResponseException("User doesn't have any roles");
                }).map(role -> {
                    return (ResponseEntity) ResponseEntity.ok(new RoleResponse(role));
                }).onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(
                        new ResponseMessage(ex.getMessage()))));
    }
}
