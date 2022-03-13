package ru.nekoguys.game.web.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.nekoguys.game.web.dto.ProfileResponse
import ru.nekoguys.game.web.dto.ProfileUpdateRequest
import ru.nekoguys.game.web.service.ProfileService
import ru.nekoguys.game.web.util.toResponseEntity
import ru.nekoguys.game.web.util.withMDCContext
import java.security.Principal

@Controller
@RequestMapping(path = ["/api/profile"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('STUDENT')")
class ProfileController(
    private val profileService: ProfileService,
) {

    @GetMapping(value = ["/navbar_info", "/get"])
    suspend fun getUser(
        principal: Principal,
    ): ResponseEntity<ProfileResponse> =
        withMDCContext {
            profileService
                .getProfile(email = principal.name)
                .toResponseEntity()
        }

    @PostMapping("/update")
    suspend fun updateUser(
        principal: Principal,
        @RequestBody profileUpdateRequest: ProfileUpdateRequest,
    ): ResponseEntity<ProfileResponse> =
        withMDCContext {
            profileService
                .updateProfile(
                    email = principal.name,
                    profileUpdateRequest = profileUpdateRequest,
                )
                .toResponseEntity(ifEmpty = HttpStatus.NOT_FOUND)
        }
}


