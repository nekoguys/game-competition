package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.RoleGetBody;
import com.groudina.ten.demo.dto.RolesInformation;
import com.groudina.ten.demo.models.DbRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity> updateRoles(Mono<Principal> principalMono, @Valid @RequestBody RolesInformation rolesInformation) {
        String email = rolesInformation.getEmail();


        return principalMono.flatMap(adminEmail -> {
            // Check if suicide.
            if (email.equals(adminEmail.getName()))
                return Mono.just(ResponseEntity.ok(new ResponseMessage("You are trying to remove your admin rights")));
            else
                return userRepository.findOneByEmail(rolesInformation.getEmail())
                        .flatMap(user ->
                                Flux.fromStream(rolesInformation.getRoles().stream())
                                        .flatMap(roleName -> rolesRepository.findByName(roleName))
                                        .collect(Collectors.toList())
                                        .flatMap(roles -> {
                                            user.setRoles(roles);
                                            ResponseEntity response = ResponseEntity.ok(new RolesInformation(convertRolesToNames(roles), email));
                                            return userRepository.save(user).thenReturn(response);
                                        })
                        ).switchIfEmpty(Mono.just(ResponseEntity.ok(new ResponseMessage("There is no user with such email"))));
        });
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> getRoles(Mono<Principal> principalMono, @Valid @RequestBody RoleGetBody roleGetBody) {
        String email = roleGetBody.getEmail();
        return userRepository.findOneByEmail(email).map(user -> {
            return new ResponseEntity(new RolesInformation(convertRolesToNames(user.getRoles()), email), HttpStatus.OK);
        }).switchIfEmpty(Mono.just(new ResponseEntity(HttpStatus.BAD_REQUEST)));
    }

    private List<String> convertRolesToNames(List<DbRole> roles) {
        return roles.stream().map(DbRole::getName).collect(Collectors.toList());
    }
}
