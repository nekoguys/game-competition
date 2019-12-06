package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.LoginUser;
import com.groudina.ten.demo.dto.NewUser;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.jwt.JWTProvider;
import com.groudina.ten.demo.jwt.JwtResponse;
import com.groudina.ten.demo.models.DbUser;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(path="/api/auth")
@Controller
public class AuthController {
    private JWTProvider jwtProvider;
    private DbUserRepository userRepository;
    private DbRolesRepository rolesRepository;
    private PasswordEncoder passwordEncoder;

    public AuthController(@Autowired DbUserRepository repository, @Autowired DbRolesRepository rolesRepository,
                          @Autowired PasswordEncoder encoder,
                          @Autowired JWTProvider jwtProvider) {
        this.userRepository = repository;
        this.passwordEncoder = encoder;
        this.jwtProvider = jwtProvider;
        this.rolesRepository = rolesRepository;
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<?>> authenticateUser(@Valid @RequestBody LoginUser loginUser) {
        return userRepository.findOneByEmail(loginUser.getEmail().get()).flatMap(user -> {
            if (passwordEncoder.matches(loginUser.getPassword().get(), user.getPassword())) {
                var jwt = jwtProvider.generateJwtToken(user.getEmail());
                List<GrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList());

                return Mono.just(ResponseEntity.ok(new JwtResponse(jwt, loginUser.getEmail().get(), authorities)));
            } else {
                return Mono.just(new ResponseEntity<>(new ResponseMessage("Invalid credentials"), HttpStatus.BAD_REQUEST));
            }
        }).switchIfEmpty(Mono.just(new ResponseEntity<>(new ResponseMessage(String.format("No user with email: %s", loginUser.getEmail().get())), HttpStatus.BAD_REQUEST)));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity> registerUser(@Valid @RequestBody NewUser newUser) {
        return userRepository.findOneByEmail(newUser.getEmail()).flatMap(user -> {
            return Mono.just(new ResponseEntity(new ResponseMessage(String.format("User with email %s already exists!", newUser.getEmail())), HttpStatus.BAD_REQUEST));
        }).switchIfEmpty(rolesRepository.findByName("ROLE_STUDENT").flatMap(role -> {
            var user = DbUser.builder().email(newUser.getEmail()).password(passwordEncoder.encode(newUser.getPassword())).build();
            user.setRoles(Arrays.asList(role));
            return userRepository.save(user).thenReturn(ResponseEntity.ok(new ResponseMessage("User registered successfully!")));
        }));
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> test() {
        return Mono.just(ResponseEntity.ok(new ResponseMessage("OK!")));
    }
}
