package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection="user")
public class DbUser {
    @Id
    private String id;

    private String email;

    private String password;

    @DBRef
    @Builder.Default
    private List<DbRole> roles = new ArrayList<>();
}
