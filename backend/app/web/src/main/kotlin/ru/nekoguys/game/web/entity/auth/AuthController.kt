package ru.nekoguys.game.web.entity.auth

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(path = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Controller
class AuthController(
    private val signInHandler: SignInHandler,
    private val signUpHandler: SignUpHandler
) {
    @PostMapping("/signin")
    suspend fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> =
        signInHandler.signIn(request)

    @PostMapping("/signup")
    suspend fun signUp(
        @RequestBody
        request: SignUpRequest,
    ): ResponseEntity<SignUpResponse> =
        signUpHandler.signUp(request)
}
