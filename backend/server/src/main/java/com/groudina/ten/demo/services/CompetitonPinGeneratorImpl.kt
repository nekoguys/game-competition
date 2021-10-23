package com.groudina.ten.demo.services

import org.springframework.stereotype.Component

@Component
class CompetitonPinGeneratorImpl : ICompetitionPinGenerator {
    override fun generate(): String {
        return (System.currentTimeMillis() - 1578000000000L).toString()
    }
}