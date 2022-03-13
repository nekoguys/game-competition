package ru.nekoguys.game.entity.commongame.service

import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository

/**
 * Генерация пин-кода игры по ID сессии c использованием модульной арифметики
 */
@Component
class SessionPinDecoder(
    private val sessionRepository: CompetitionSessionRepository,
) {

    suspend fun decodeIdFromPin(sessionPin: String): CommonSession.Id? =
        decodeIdFromPinUnsafe(sessionPin)
            ?.let {
                sessionRepository
                    .findAll(listOf(it), fieldSelectors = emptySet())
                    .singleOrNull()
                    ?.let(CompetitionSession::id)
            }

    fun decodeIdFromPinUnsafe(sessionPin: String): Long? {
        val pin = sessionPin.toBigIntegerOrNull() ?: return null
        val pinModulo = pin % MODULO
        val idModulo = pinModulo * MULTIPLIER_INVERT % MODULO
        return (pin - pinModulo + idModulo).toLong()
    }

    companion object {
        val MODULO = 10000.toBigInteger()
        val MULTIPLIER = 647.toBigInteger()
        val MULTIPLIER_INVERT = 2983.toBigInteger()
    }
}

fun CommonSession.Id.toPin(): String {
    val id = this.number.toBigInteger()
    val idModulo = id % SessionPinDecoder.MODULO
    val pinModulo = idModulo * SessionPinDecoder.MULTIPLIER % SessionPinDecoder.MODULO
    return (id - idModulo + pinModulo).toString()
}

val CompetitionSession.pin: String
    get() = id.toPin()
