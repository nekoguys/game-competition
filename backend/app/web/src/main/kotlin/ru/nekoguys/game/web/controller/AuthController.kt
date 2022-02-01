package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.nekoguys.game.web.dto.SignInRequest
import ru.nekoguys.game.web.dto.SignInResponse
import ru.nekoguys.game.web.dto.SignUpRequest
import ru.nekoguys.game.web.dto.SignUpResponse
import ru.nekoguys.game.web.service.AuthService
import ru.nekoguys.game.web.util.withMDCContext

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(path = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Controller
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signin")
    suspend fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> =
        withMDCContext { authService.signIn(request) }

    @PostMapping("/signup")
    suspend fun signUp(
        @RequestBody
        request: SignUpRequest,
    ): ResponseEntity<SignUpResponse> =
        withMDCContext { authService.signUp(request) }
}
