package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbRole;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IRolesMapper {
    Mono<List<DbRole>> map(String role);
}
