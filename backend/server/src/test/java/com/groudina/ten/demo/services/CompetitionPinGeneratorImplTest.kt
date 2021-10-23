package com.groudina.ten.demo.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CompetitionPinGeneratorImplTest {

    private val competitonPinGenerator: ICompetitionPinGenerator = CompetitonPinGeneratorImpl()

    @Test
    fun generate() {
        val firstPin = competitonPinGenerator.generate()
        assertThat(firstPin)
            .containsOnlyDigits()
            .isNotEqualTo("0")

        Thread.sleep(2000)

        val secondPin = competitonPinGenerator.generate()
        assertThat(secondPin)
            .isGreaterThan(firstPin)
    }
}