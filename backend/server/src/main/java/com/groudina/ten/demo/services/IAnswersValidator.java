package com.groudina.ten.demo.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface IAnswersValidator {

    AnswerValidationResultDto validateAnswer(AnswerValidationRequestDto answerValidationRequestDto);

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    static class AnswerValidationResultDto {
        private boolean isOk;
        private String message;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    static class AnswerValidationRequestDto {
        private int answer;
    }
}
