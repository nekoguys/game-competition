package ru.nekoguys.game.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication(
    scanBasePackages = ["ru.nekoguys.game"]
)
class GameWebApplication

fun main(args: Array<String>) {
//    ReactorDebugAgent.init()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<GameWebApplication>(*args)
}
