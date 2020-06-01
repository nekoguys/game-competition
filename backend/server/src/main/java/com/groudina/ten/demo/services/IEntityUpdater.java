package com.groudina.ten.demo.services;

import reactor.core.publisher.Mono;

public interface IEntityUpdater<T, U> {
    Mono<T> update(T entity, U update);
}
