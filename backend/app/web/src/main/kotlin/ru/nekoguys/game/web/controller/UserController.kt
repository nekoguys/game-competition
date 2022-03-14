package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.nekoguys.game.web.dto.UserResponse
import ru.nekoguys.game.web.dto.UserSearchRequest
import ru.nekoguys.game.web.dto.UserSearchResponse
import ru.nekoguys.game.web.dto.UserUpdateRequest
import ru.nekoguys.game.web.service.UserService
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@Controller
@RequestMapping(path = ["/api/user"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('STUDENT')")
class UserController(
    private val userService: UserService,
) {

    @GetMapping(value = ["/navbar_info", "/current"])
    suspend fun currentUser(
        principal: Principal,
    ): ResponseEntity<UserResponse> =
        wrapServiceCall {
            userService
                .getUser(
                    email = principal.name,
                )
        }

    @PostMapping("/get")
    suspend fun findUser(
        principal: Principal,
        @RequestBody targetEmail: String, // TODO: findUserRequest (because json)
    ): ResponseEntity<UserResponse> =
        wrapServiceCall {
            userService
                .getUser(
                    email = targetEmail,
                )
        }

    @PostMapping(value = ["/update", "/update_role"])
    suspend fun updateUser(
        principal: Principal,
        @RequestBody userUpdateRequest: UserUpdateRequest,
    ): ResponseEntity<UserResponse> =
        wrapServiceCall {
            userService
                .updateUser(
                    operatorEmail = principal.name,
                    userUpdateRequest = userUpdateRequest,
                )
        }

    @PostMapping(value = ["/search"])
    suspend fun searchUser(
        principal: Principal,
        @RequestBody userSearchRequest: UserSearchRequest,
    ): ResponseEntity<UserSearchResponse> =
        wrapServiceCall {
            userService
                .findUsers(
                    userSearchRequest = userSearchRequest
                )
        }
}
