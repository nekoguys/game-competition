package ru.nekoguys.game.web.util

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

open class WebResponse(
    @get:JsonIgnore
    val status: HttpStatus
)

fun <T : WebResponse> T?.toResponseEntity(
    notFoundStatus: HttpStatus = HttpStatus.NOT_FOUND,
): ResponseEntity<T> =
    if (this != null) {
        ResponseEntity.status(status).body(this)
    } else {
        ResponseEntity.status(notFoundStatus).build()
    }
