package com.groudina.ten.demo.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswersValidatorImplTest {

    @Test
    void validateAnswer() {
        var validator = new AnswersValidatorImpl();

        assertTrue(validator.validateAnswer(IAnswersValidator.AnswerValidationRequestDto.builder().answer(10).build()).isOk());
        assertFalse(validator.validateAnswer(IAnswersValidator.AnswerValidationRequestDto.builder().answer(-1).build()).isOk());
        assertFalse(validator.validateAnswer(IAnswersValidator.AnswerValidationRequestDto.builder().answer(10000).build()).isOk());
    }
}