package ru.selemilka.game.rps.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.selemilka.game.core.session.LoggedGameCommand
import ru.selemilka.game.core.session.LoggedGameMessage
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsMessage

object RpsLoggedGameMessageSerializer : KSerializer<LoggedGameMessage<RpsPlayer.Human, RpsMessage>> {
    override val descriptor: SerialDescriptor =
        Surrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LoggedGameMessage<RpsPlayer.Human, RpsMessage>) {
        val surrogate = Surrogate(
            players = value.players,
            messageBody = value.messageBody,
        )
        encoder.encodeSerializableValue(
            serializer = Surrogate.serializer(),
            value = surrogate,
        )
    }

    override fun deserialize(decoder: Decoder): LoggedGameMessage<RpsPlayer.Human, RpsMessage> {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return LoggedGameMessage(
            players = surrogate.players,
            messageBody = surrogate.messageBody,
        )
    }

    @Serializable
    @SerialName("LoggedGameMessage")
    private class Surrogate(
        val players: Set<RpsPlayer.Human>,
        val messageBody: @Polymorphic RpsMessage,
    )
}

object RpsLoggedGameCommandSerializer : KSerializer<LoggedGameCommand<RpsPlayer, RpsCommand>> {
    override val descriptor: SerialDescriptor =
        Surrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LoggedGameCommand<RpsPlayer, RpsCommand>) {
        val surrogate = Surrogate(
            player = value.player,
            command = value.command,
        )
        encoder.encodeSerializableValue(
            serializer = Surrogate.serializer(),
            value = surrogate,
        )
    }

    override fun deserialize(decoder: Decoder): LoggedGameCommand<RpsPlayer, RpsCommand> {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return LoggedGameCommand(
            player = surrogate.player,
            command = surrogate.command,
        )
    }

    @Serializable
    @SerialName("LoggedGameMessage")
    private class Surrogate(
        val player: RpsPlayer,
        val command: RpsCommand,
    )
}
