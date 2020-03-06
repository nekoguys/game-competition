package com.groudina.ten.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRoundInfoSchedulerDto {
    private String id;
    private long startTime;
    private int additionalMinutes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetitionRoundInfoSchedulerDto that = (CompetitionRoundInfoSchedulerDto) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
