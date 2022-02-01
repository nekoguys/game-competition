package ru.nekoguys.game.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["ru.nekoguys.game"]
)
class GameWebApplication

fun main(args: Array<String>) {
    runApplication<GameWebApplication>(*args)
}
