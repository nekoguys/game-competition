package com.groudina.ten.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "verification_token")
public class DbVerificationToken {
    @Id
    private String id;

    private String token;

    @DBRef
    private DbUser user;
}
