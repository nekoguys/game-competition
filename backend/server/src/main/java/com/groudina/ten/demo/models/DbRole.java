package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection="role")
public class DbRole {
    @Id
    private String id;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbRole dbRole = (DbRole) o;
        return Objects.equals(id, dbRole.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
