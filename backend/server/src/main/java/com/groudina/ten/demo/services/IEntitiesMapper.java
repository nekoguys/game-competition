package com.groudina.ten.demo.services;

import org.springframework.data.util.Pair;

public interface IEntitiesMapper<T, U> {
    U map(T from, Iterable<Pair<String, ?>> additionalFields);
}
