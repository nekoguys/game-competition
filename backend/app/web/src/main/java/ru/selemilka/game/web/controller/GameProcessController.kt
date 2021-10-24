package ru.selemilka.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(path = ["/game"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GameProcessController {
    @RequestMapping("/hello")
    suspend fun helloWorld() = ResponseEntity.ok("Hello world!")
}