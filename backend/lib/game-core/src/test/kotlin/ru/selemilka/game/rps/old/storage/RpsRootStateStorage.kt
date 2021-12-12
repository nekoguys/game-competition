package ru.selemilka.game.rps.old.storage
//
//import ru.selemilka.game.core.base.SessionId
//
//interface RpsRootStageStorage {
//    sealed interface Stage
//
//    object JoiningStage: Stage
//    object GameStage: Stage
//    object CompletionStage: Stage
//
//    fun stageFor(id: SessionId): Stage?
//    fun initGame(id: SessionId)
//    fun joiningEnded(id: SessionId)
//    fun gameEnded(id: SessionId)
//}
//
//class RpsRootStateInMemoryStorage: RpsRootStageStorage {
//    private val stages = mutableMapOf<SessionId, RpsRootStageStorage.Stage>()
//
//    override fun stageFor(id: SessionId): RpsRootStageStorage.Stage? {
//        return stages[id]
//    }
//
//    override fun initGame(id: SessionId) {
//        stages[id] = RpsRootStageStorage.JoiningStage
//    }
//
//    override fun joiningEnded(id: SessionId) {
//        stages[id] = RpsRootStageStorage.GameStage
//    }
//
//    override fun gameEnded(id: SessionId) {
//        stages[id] = RpsRootStageStorage.CompletionStage
//    }
//}
