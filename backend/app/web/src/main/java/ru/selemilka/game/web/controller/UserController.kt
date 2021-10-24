package ru.selemilka.game.web.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.repository.DbUserRepository

@Controller
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(path = ["/user/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    // надо сделать так, чтобы из web нельзя было дёргать методы репозиториев напрямую
    // кажется, для этого нужно в мавене пометить зависимость как runtime
    val dbUserRepository: DbUserRepository,
) {
    @RequestMapping("/create/{email}/{role}")
    suspend fun create(
        @PathVariable id: Long,
        @PathVariable email: String,
        @PathVariable role: String,
    ) {
        val user = DbUser(id, email, role)
        dbUserRepository.save(user)
    }

    @RequestMapping("/find")
    suspend fun find(
        @PathVariable id: Long,
    ): ResponseEntity<DbUser> {
        val user = dbUserRepository.findById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
