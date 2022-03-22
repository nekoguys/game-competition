package ru.nekoguys.game.web.util

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent

open class WebResponse(
    @get:JsonIgnore
    val status: HttpStatus
)

fun <T : Any> Flow<T>.asServerSentEventStream(
    streamName: String,
): Flow<ServerSentEvent<T>> =
    map { ServerSentEvent.builder(it).id(streamName).build() }
        .onEach {
            logger.debug("Sent event $it")
        }
        .withRequestIdInContext()

suspend fun <T : WebResponse> wrapServiceCall(block: suspend () -> T?): ResponseEntity<T> {
    return withMDCContext {
        @Suppress("DEPRECATION")
        block()
            .toResponseEntity()
            .also { logger.debug("Got response $it") }
    }
}

@Deprecated(
    message = "It's probably better to use `wrapServiceCall` there",
    replaceWith = ReplaceWith(""),
    level = DeprecationLevel.WARNING,
)
fun <T : WebResponse> T?.toResponseEntity(
    ifEmpty: HttpStatus = HttpStatus.BAD_REQUEST,
): ResponseEntity<T> =
    if (this != null) {
        ResponseEntity.status(status).body(this)
    } else {
        ResponseEntity.status(ifEmpty).build()
    }

private val logger = LoggerFactory.getLogger("ru.nekoguys.game.web.util.WebResponseUtilsKt")
