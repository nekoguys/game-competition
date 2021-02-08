package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.ChangeUserPasswordRequestDto;
import com.groudina.ten.demo.dto.NewUserWithRole;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.models.DbUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(path = "/api/users", produces = {MediaType.APPLICATION_JSON_VALUE})
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);

    private DbUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private DbRolesRepository rolesRepository;

    public UserController(@Autowired DbUserRepository userRepository,
                          @Autowired PasswordEncoder passwordEncoder,
                          @Autowired DbRolesRepository rolesRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolesRepository = rolesRepository;
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ResponseMessage>> createUser(@Valid @RequestBody NewUserWithRole newUser) {
        log.info("POST: /api/users/create, body: {}", newUser);
        return userRepository.findOneByEmail(newUser.getEmail())
                .flatMap(user -> Mono.just(new ResponseEntity<>(new ResponseMessage(
                        String.format("User with email '%s' already exists!", newUser.getEmail())), HttpStatus.UNPROCESSABLE_ENTITY)))
                .switchIfEmpty(Flux.fromStream(getAllRoles(newUser.getRole()))
                        .flatMap(role -> rolesRepository.findByName(role))
                        .collect(Collectors.toList())
                        .flatMap(roles -> {
                            DbUser user = DbUser.builder().email(newUser.getEmail())
                                    .password(passwordEncoder.encode(newUser.getPassword())).roles(roles).build();

                            return userRepository.save(user)
                                    .thenReturn(ResponseEntity.ok(new ResponseMessage("User created successfully!")));
                        }));
    }

    @PostMapping(value = "/change_pwd")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ResponseMessage>> changeUserPwd(@Valid @RequestBody ChangeUserPasswordRequestDto dto) {
        log.info("POST: /api/users/change_pwd, body: {}", dto);
        return userRepository.findOneByEmail(dto.getUserEmail())
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

                    return userRepository.save(user).thenReturn(ResponseEntity.ok(new ResponseMessage("Changed password successfully")));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.ok(new ResponseMessage("No such user"))));
    }

    private Stream<String> getAllRoles(String topRoleName) {
        switch (topRoleName) {
            case "ROLE_ADMIN":
                return Stream.of("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT");
            case "ROLE_TEACHER":
                return Stream.of("ROLE_TEACHER", "ROLE_STUDENT");
            default:
                return Stream.of("ROLE_STUDENT");
        }
    }
}