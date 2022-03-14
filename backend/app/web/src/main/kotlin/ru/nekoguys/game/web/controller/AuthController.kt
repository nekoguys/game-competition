package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nekoguys.game.web.dto.SignInRequest
import ru.nekoguys.game.web.dto.SignInResponse
import ru.nekoguys.game.web.dto.SignUpRequest
import ru.nekoguys.game.web.dto.SignUpResponse
import ru.nekoguys.game.web.service.AuthService
import ru.nekoguys.game.web.util.wrapServiceCall

@RestController
@RequestMapping(path = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signin")
    suspend fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> =
        wrapServiceCall {
            authService
                .signIn(request)
        }

    @PostMapping("/signup")
    suspend fun signUp(
        @RequestBody request: SignUpRequest,
    ): ResponseEntity<out SignUpResponse> =
        wrapServiceCall {
            authService
                .signUp(request)
        }
}
