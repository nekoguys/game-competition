package ru.nekoguys.game.core.rps

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RpsGameConfiguration::class])
annotation class RpsGameCoreTest

@Configuration
@ComponentScan
class RpsGameConfiguration
