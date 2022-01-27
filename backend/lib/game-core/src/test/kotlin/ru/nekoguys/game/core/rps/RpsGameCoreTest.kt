package ru.nekoguys.game.core.rps

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [RpsGameConfiguration::class])
annotation class RpsGameCoreTest

@Configuration
@ComponentScan
@EnableConfigurationProperties
class RpsGameConfiguration
