package com.groudina.ten.demo.db;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.models.DbCompetition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableEmbeddedMongo
public class DbCompetitionRepositoryTest {
    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @Test
    void testAutomaticAndActive() {
        competitionsRepository.save(DbCompetition.builder().pin("1").state(DbCompetition.State.InProcess)
                .parameters(DbCompetition.Parameters.builder()
                        .isAutoRoundEnding(true)
                        .build())
                .build()).block();
        competitionsRepository.save(DbCompetition.builder().pin("2").state(DbCompetition.State.InProcess)
                .parameters(DbCompetition.Parameters.builder()
                        .isAutoRoundEnding(false)
                        .build())
                .build()).block();
        competitionsRepository.save(DbCompetition.builder().pin("3").state(DbCompetition.State.Ended)
                .parameters(DbCompetition.Parameters.builder()
                        .isAutoRoundEnding(true)
                        .build())
                .build()).block();

        var lst = competitionsRepository.findAllByParameters_IsAutoRoundEndingTrueAndState(DbCompetition.State.InProcess).collectList().block();
        assertEquals(lst.size(), 1);
        assertEquals(lst.get(0).getPin(), "1");
    }

}
