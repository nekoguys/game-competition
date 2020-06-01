package com.groudina.ten.demo.services;

import org.springframework.stereotype.Component;

@Component
public class EmailValidatorImpl implements IEmailValidator {
    private static final String TEACHER_SUFFIX = "@hse.ru";
    private static final String STUDENT_SUFFIX = "@edu.hse.ru";
    @Override
    public boolean validateEmail(String email) {
        if (email.endsWith(STUDENT_SUFFIX)) {
            return (email.length() >= STUDENT_SUFFIX.length() + 3);
        } else if (email.endsWith(TEACHER_SUFFIX)) {
            return (email.length() >= TEACHER_SUFFIX.length() + 3);
        }
        return false;
    }
}
