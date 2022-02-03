package ru.nekoguys.game.entity.commongame.service

import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.model.CommonSession

/**
 * Генерация пин-кода игры по ID сессии c использованием модульной арифметики
 */
@Component
class SessionPinGenerator {
    fun convertSessionIdToPin(sessionId: CommonSession.Id): Long {
        val id = sessionId.number.toBigInteger()
        val idModulo = id % MODULO
        val pinModulo = idModulo * MULTIPLIER % MODULO
        return (id - idModulo + pinModulo).toLong()
    }

    fun decodeIdFromPin(sessionPin: Long): Long {
        val pin = sessionPin.toBigInteger()
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
