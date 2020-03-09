package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.LoginUser;
import com.groudina.ten.demo.dto.NewUser;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.jwt.JWTProvider;
import com.groudina.ten.demo.jwt.JwtResponse;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IEmailService;
import com.groudina.ten.demo.services.IEmailValidator;
import com.groudina.ten.demo.services.IVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RequestMapping(path="/api/auth", produces = {MediaType.APPLICATION_JSON_VALUE})
@Controller
public class AuthController {
    private JWTProvider jwtProvider;
    private DbUserRepository userRepository;
    private DbRolesRepository rolesRepository;
    private PasswordEncoder passwordEncoder;
    private IEmailValidator emailValidator;
    private IEmailService emailService;
    private IVerificationTokenService verificationTokenService;

    public AuthController(@Autowired DbUserRepository repository, @Autowired DbRolesRepository rolesRepository,
                          @Autowired PasswordEncoder encoder, @Autowired JWTProvider jwtProvider,
                          @Autowired IEmailValidator emailValidator, @Autowired IEmailService emailService,
                          @Autowired IVerificationTokenService verificationTokenService) {
        this.userRepository = repository;
        this.passwordEncoder = encoder;
        this.jwtProvider = jwtProvider;
        this.rolesRepository = rolesRepository;
        this.emailValidator = emailValidator;
        this.emailService = emailService;
        this.verificationTokenService = verificationTokenService;
    }

    @GetMapping(value="/test_email")
    public Mono<ResponseEntity<ResponseMessage>> testEmail() {
        return this.emailService.sendEmail("s18b3_benua@179.ru", "https://google.com")
                .then(Mono.just(ResponseEntity.ok(ResponseMessage.of("success"))))
                .onErrorResume(ex -> {
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity.ok(ResponseMessage.of("failure\n" + ex.getMessage())));
                });
    }


    @PostMapping(value="/signin")
    public Mono<ResponseEntity<? extends Serializable>> authenticateUser(@Valid @RequestBody LoginUser loginUser) {
        return userRepository.findOneByEmail(loginUser.getEmail()).flatMap(user -> {
            if (passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
                if (user.isVerified()) {
                    var jwt = jwtProvider.generateJwtToken(user.getEmail());
                    List<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName()))
                            .collect(Collectors.toList());

                    return Mono.just(ResponseEntity.ok(new JwtResponse(jwt, loginUser.getEmail(), authorities)));
                } else {
                    return Mono.just(ResponseEntity.badRequest().body(new ResponseMessage("Your account is not verified")));
                }
            } else {
                return Mono.just(new ResponseEntity<>(new ResponseMessage("Invalid credentials"), HttpStatus.BAD_REQUEST));
            }
        }).switchIfEmpty(Mono.just(new ResponseEntity<>(new ResponseMessage(String.format("No user with email: %s", loginUser.getEmail())), HttpStatus.BAD_REQUEST)));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity> registerUser(@Valid @RequestBody NewUser newUser) {
        if (emailValidator.validateEmail(newUser.getEmail())) {
            return userRepository.findOneByEmail(newUser.getEmail()).flatMap(user -> {
                return Mono.just(new ResponseEntity(new ResponseMessage(String.format("User with email %s already exists!", newUser.getEmail())), HttpStatus.BAD_REQUEST));
            }).switchIfEmpty(rolesRepository.findByName("ROLE_STUDENT").flatMap(role -> {
                var user = DbUser.builder().email(newUser.getEmail()).isVerified(false).password(passwordEncoder.encode(newUser.getPassword())).build();
                user.setRoles(Arrays.asList(role));
                return userRepository.save(user)
                        .flatMap(savedUser -> {
                            return this.verificationTokenService.createVerificationToken(user);
                        }).thenReturn(ResponseEntity.ok(new ResponseMessage("User registered successfully!")));
            }));
        } else {
            return Mono.just(new ResponseEntity(new ResponseMessage("Invalid email. Email should end with @edu.hse.ru or @hse.ru"), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/verification/{token}")
    public Mono<ResponseEntity<ResponseMessage>> verifyUser(@PathVariable String token) {
        return this.verificationTokenService.verifyUser(token).map(__ -> {
            return ResponseEntity.ok(ResponseMessage.of("Verified successfully"));
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("Token not found")));
        }));
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<?>> test() {
        return Mono.just(ResponseEntity.ok(new ResponseMessage("OK!")));
    }
}
