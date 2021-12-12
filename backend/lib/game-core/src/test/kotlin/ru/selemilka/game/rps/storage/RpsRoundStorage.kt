package ru.selemilka.game.rps.storage

import org.springframework.stereotype.Repository
import ru.selemilka.game.rps.model.RpsRound
import ru.selemilka.game.rps.model.RpsSession

interface RpsRoundStorage {
    suspend fun loadCurrentRound(sessionId: RpsSession.Id): RpsRound?
    suspend fun loadRound(roundId: RpsRound.Id): RpsRound?
    suspend fun saveRound(round: RpsRound)
}

@Repository
class RpsRoundInMemoryStorage : RpsRoundStorage {
    private val rounds = mutableMapOf<RpsRound.Id, RpsRound>()
    private val currentRoundIds = mutableMapOf<RpsSession.Id, RpsRound.Id>()

    override suspend fun loadCurrentRound(sessionId: RpsSession.Id): RpsRound? {
        val currentRoundId = currentRoundIds[sessionId] ?: return null
        return rounds[currentRoundId]
    }

    override suspend fun loadRound(roundId: RpsRound.Id): RpsRound? {
        return rounds[roundId]
    }

    override suspend fun saveRound(round: RpsRound) {
        val currentRoundNumber = currentRoundIds[round.id.sessionId]?.number
        require(currentRoundNumber == null || round.id.number > currentRoundNumber) {
            "The past cannot be changed, kiddo"
        }
        rounds[round.id] = round
        currentRoundIds[round.id.sessionId] = round.id
    }
}
