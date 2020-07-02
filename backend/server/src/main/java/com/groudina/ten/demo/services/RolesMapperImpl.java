package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.exceptions.ResponseException;
import com.groudina.ten.demo.models.DbRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolesMapperImpl implements IRolesMapper {
    private DbRolesRepository rolesRepository;


    public RolesMapperImpl(@Autowired DbRolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    private List<String> getRolesNamesUpTo(String topRoleName) {
        switch (topRoleName) {
            case "ROLE_ADMIN":
                return List.of("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT");
            case "ROLE_TEACHER":
                return List.of("ROLE_TEACHER", "ROLE_STUDENT");
            case "ROLE_STUDENT":
                return List.of("ROLE_STUDENT");
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public Mono<List<DbRole>> map(String topRoleName) {
        List<String> roleNames = getRolesNamesUpTo(topRoleName);

        Mono<List<DbRole>> roleNotFoundFallback = Mono.error(
                new ResponseException(String.format("Role \"%s\" doesn't exist", topRoleName)));
        return Flux.fromIterable(roleNames)
                .flatMap(roleName -> rolesRepository.findByName(roleName))
                .collectList()
                .flatMap(list -> list.isEmpty() ? roleNotFoundFallback : Mono.just(list))
                .switchIfEmpty(roleNotFoundFallback);
    }

    @Override
    public String getTopRoleName(List<DbRole> roles) {
        var roleNames = roles.stream()
                .map(DbRole::getName)
                .collect(Collectors.toList());

        if (roleNames.contains("ROLE_ADMIN"))
            return "ROLE_ADMIN";
        if (roleNames.contains("ROLE_TEACHER"))
            return "ROLE_TEACHER";
        if (roleNames.contains("ROLE_STUDENT"))
            return "ROLE_STUDENT";

        throw new ResponseException("User doesn't have any roles");
    }
}
