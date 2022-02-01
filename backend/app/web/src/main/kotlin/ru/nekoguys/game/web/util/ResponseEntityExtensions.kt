package ru.nekoguys.game.web.util

import org.springframework.http.ResponseEntity

fun <T> notFoundResponse(): ResponseEntity<T> =
    ResponseEntity.notFound().build()

fun <T> T.toOkResponse(): ResponseEntity<T> =
    ResponseEntity.ok(this)

fun <T> T.toBadRequestResponse(): ResponseEntity<T> =
    ResponseEntity.badRequest().body(this)
