package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NavBarInfoResponse;
import com.groudina.ten.demo.dto.ProfileInfoResponseDto;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.UpdateProfileRequestDto;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IEntitiesMapper;
import com.groudina.ten.demo.services.IEntityUpdater;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;

@Log4j2
@RequestMapping(path="/api/profile", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
public class ProfileController {
    private DbUserRepository userRepository;
    private IEntitiesMapper<DbUser, ProfileInfoResponseDto> profileInfoMapper;
    private IEntityUpdater<DbUser, UpdateProfileRequestDto> userUpdater;
    private IEntitiesMapper<DbUser, NavBarInfoResponse> navbarInfoMapper;

    public ProfileController(
            @Autowired DbUserRepository userRepository,
            @Autowired IEntitiesMapper<DbUser, ProfileInfoResponseDto> profileInfoMapper,
            @Autowired IEntityUpdater<DbUser, UpdateProfileRequestDto> userUpdater,
            @Autowired IEntitiesMapper<DbUser, NavBarInfoResponse> navbarInfoMapper
    ) {
        this.userRepository = userRepository;
        this.profileInfoMapper = profileInfoMapper;
        this.userUpdater = userUpdater;
        this.navbarInfoMapper = navbarInfoMapper;
    }


    @GetMapping("/navbar_info")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> getNavBarInfo(Mono<Principal> principalMono) {
        return principalMono.flatMap(principal -> {
            return userRepository.findOneByEmail(principal.getName());
        }).map(user -> {
            return (ResponseEntity)ResponseEntity.ok(navbarInfoMapper.map(user, null));
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("User not found")));
        }));
    }

    @GetMapping("/get")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> getProfileInfo(Mono<Principal> principalMono) {
        return principalMono.flatMap(principal -> {
            return userRepository.findOneByEmail(principal.getName());
        }).map(user -> {
            return (ResponseEntity)ResponseEntity.ok(
                    profileInfoMapper.map(user, null)
            );
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("User not found")));
        }));
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> updateProfile(Mono<Principal> principalMono, @RequestBody @Valid UpdateProfileRequestDto dto) {
        return principalMono.flatMap(principal -> {
            return userRepository.findOneByEmail(principal.getName());
        }).flatMap(user -> {
            return userUpdater.update(user, dto);
        }).flatMap(user -> {
            return userRepository.save(user);
        }).then(Mono.defer(() -> {
            return Mono.just((ResponseEntity)ResponseEntity.ok(ResponseMessage.of("Profile saved successfully")));
        })).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("User not found")));
        }));
    }
}
