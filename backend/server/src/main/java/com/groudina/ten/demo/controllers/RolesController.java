package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.RoleGetRequest;
import com.groudina.ten.demo.dto.RolePutRequest;
import com.groudina.ten.demo.dto.RolesResponse;
import com.groudina.ten.demo.models.DbRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping(path = "/api/roles")
@Controller
public class RolesController {
    private DbUserRepository userRepository;
    private DbRolesRepository rolesRepository;

    public RolesController(
            @Autowired DbUserRepository userRepository,
            @Autowired DbRolesRepository rolesRepository) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
    }

    @PutMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity> updateRoles(Mono<Principal> principalMono, @Valid @RequestBody RolePutRequest rolePutRequest) {
        String email = rolePutRequest.getEmail();
        return principalMono.flatMap(adminEmail -> {
            // Check if admin commits suicide.
            if (email.equals(adminEmail.getName()) && !rolePutRequest.getRole().equals("ROLE_ADMIN"))
                return Mono.just(ResponseEntity.ok(new ResponseMessage("You are not allowed to remove your admin rights")));
            else
                return userRepository.findOneByEmail(rolePutRequest.getEmail())
                        .flatMap(user ->
                                Flux.fromStream(getNeededRoles(rolePutRequest.getRole()))
                                        .flatMap(roleName -> rolesRepository.findByName(roleName))
                                        .collect(Collectors.toList())
                                        .flatMap(roles -> {
                                            user.setRoles(roles);
                                            ResponseEntity response = ResponseEntity.ok(new RolesResponse(convertRolesToNames(roles), email));
                                            return userRepository.save(user).thenReturn(response);
                                        }).switchIfEmpty(Mono.just(ResponseEntity.ok(new ResponseMessage(
                                        String.format("Role %s not found", rolePutRequest.getRole())))))
                        ).switchIfEmpty(Mono.just(ResponseEntity.ok(new ResponseMessage("There is no user with such email"))));
        });
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity> getNeededRoles(@Valid @RequestBody RoleGetRequest roleGetRequest) {
        String email = roleGetRequest.getEmail();
        return userRepository.findOneByEmail(email).map(user -> {
            return new ResponseEntity(new RolesResponse(convertRolesToNames(user.getRoles()), email), HttpStatus.OK);
        }).switchIfEmpty(Mono.just(new ResponseEntity(HttpStatus.BAD_REQUEST)));
    }

    private List<String> convertRolesToNames(List<DbRole> roles) {
        return roles.stream().map(DbRole::getName).collect(Collectors.toList());
    }

    private Stream<String> getNeededRoles(String topRoleName) {
        switch (topRoleName) {
            case "ROLE_ADMIN":
                return Stream.of("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT");
            case "ROLE_TEACHER":
                return Stream.of("ROLE_TEACHER", "ROLE_STUDENT");
            case "ROLE_STUDENT":
                return Stream.of("ROLE_STUDENT");
            default:
                return Stream.of(topRoleName);
        }
    }
}
