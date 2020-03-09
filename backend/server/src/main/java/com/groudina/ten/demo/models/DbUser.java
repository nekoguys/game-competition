package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Builder.Default
    private boolean isVerified = true;

    @DBRef
    @Builder.Default
    private List<DbRole> roles = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbUser dbUser = (DbUser) o;
        return getId().equals(dbUser.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
