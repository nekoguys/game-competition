package ru.selemilka.game.competition.process

import org.springframework.stereotype.Component
import ru.selemilka.game.competition.Competition
import ru.selemilka.game.core.model.GameAction
import ru.selemilka.game.core.process.GameProcess

@Component
class CompetitionProcess : GameProcess<Competition>() {
    override suspend fun process(gameAction: GameAction<Competition>) {
        TODO("Not yet implemented")
    }
}