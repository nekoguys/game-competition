package ru.selemilka.game.rps.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.rule.*

// TODO: может быть можно как-то обойтись без перечисления всех подклассов
//  например, можно сериализовать GameMessage<RpsPlayer, RpsMessage> как какую-то другую структуру
val format = Json {
    serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(RpsPlayer.Human::class)
            subclass(RpsPlayer.Internal::class)

            // join game
            subclass(RpsJoinGameMessage.YouJoinedGame::class)
            subclass(RpsJoinGameMessage.SomebodyJoinedGame::class)
            subclass(JoinGameMessageError.YouAlreadyJoined::class)
            subclass(JoinGameMessageError.SessionIsFull::class)

            // change stage
            subclass(RpsChangeStageMessage.GameStarted::class)
            subclass(RpsChangeStageMessage.GameFinished::class)

            // submit answer
            subclass(RpsSubmitAnswerMessage.Submitted::class)
            subclass(RpsSubmitAnswerMessage.RoundEnded::class)
            subclass(SubmitAnswerMessageError.AnswerAlreadySubmitted::class)
        }
    }
}
