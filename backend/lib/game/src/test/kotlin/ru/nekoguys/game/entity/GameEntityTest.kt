package ru.nekoguys.game.entity

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GameEntityTestConfiguration::class])
annotation class GameEntityTest

@Configuration
@ComponentScan
class GameEntityTestConfiguration
