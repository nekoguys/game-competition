package com.groudina.ten.demo.services;

import org.springframework.stereotype.Service;

@Service
public class AnswersValidatorImpl implements IAnswersValidator {
    private final int ANSWER_UPPERBOUND = 10000;
    private final int ANSWER_LOWERBOUND = 0;

    @Override
    public AnswerValidationResultDto validateAnswer(AnswerValidationRequestDto answerValidationRequestDto) {
        if (answerValidationRequestDto.getAnswer() > ANSWER_LOWERBOUND
                && answerValidationRequestDto.getAnswer() < ANSWER_UPPERBOUND) {
            return AnswerValidationResultDto.builder().isOk(true).build();
        } else {
            return AnswerValidationResultDto.builder().isOk(false).message("Your answer is too small or too big").build();
        }
    }
}
