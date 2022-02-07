package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal

@Controller
@RequestMapping(path = ["/game"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GameProcessController {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hello")
    suspend fun helloWorld(
        principal: Principal,
    ) = ResponseEntity.ok("Hello world!")

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/error")
    suspend fun error(): ResponseEntity<*> = TODO()
}
