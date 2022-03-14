package ru.nekoguys.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.service.UserService
import ru.nekoguys.game.web.util.wrapServiceCall
import java.security.Principal

@Controller
@RequestMapping(path = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('STUDENT')")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    suspend fun getCurrent(
        principal: Principal,
    ): ResponseEntity<UserApiResponse<UserResponse>> =
        wrapServiceCall {
            userService
                .getUser(
                    operatorEmail = principal.name,
                    targetEmail = principal.name,
                )
        }

    @PostMapping("/find_by_email")
    suspend fun findByEmail(
        principal: Principal,
        @RequestBody request: FindUserByEmailRequest,
    ): ResponseEntity<UserApiResponse<UserResponse>> =
        wrapServiceCall {
            userService
                .getUser(
                    operatorEmail = principal.name,
                    targetEmail = request.email,
                )
        }

    @PostMapping("/update")
    suspend fun update(
        principal: Principal,
        @RequestBody userUpdateRequest: UserUpdateRequest,
    ): ResponseEntity<UserApiResponse<UserResponse>> =
        wrapServiceCall {
            userService
                .updateUser(
                    operatorEmail = principal.name,
                    request = userUpdateRequest,
                )
        }

    @PostMapping("/find_by_filter")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun findByFilter(
        principal: Principal,
        @RequestBody request: FindUsersByFilterRequest,
    ): ResponseEntity<UserSearchResponse> =
        wrapServiceCall {
            userService
                .findUsers(
                    query = request.query,
                    page = request.page,
                    pageSize = request.pageSize,
                )
        }
}
