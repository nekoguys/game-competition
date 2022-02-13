package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.user.model.User
import java.time.LocalDateTime

/**
 * Такая сложная модель данных позволяет загружать из БД только нужные поля:
 *
 * ```
 * val sessionId = competitionSessionRepository.create(
 *     stage = CompetitionStage.Registration,
 *     creatorId = User.Id(13),
 *     settings = CompetitionSettings.Default,
 * )
 *
 * val session = competitionSessionRepository.load(
 *     id = sessionId,
 *     CompetitionSession.WithSettings,
 *     CompetitionSession.WithTeams,
 * )
 * val settings = session.settings
 * val teams = session.teams
 * // val creatorId = session.creatorId - не компилируется, так как это поле не запрашивалось
 *
 * competitionSessionRepository.update(
 *     oldSession = session,
 *     newSession = session.copy(
 *         settings = settings.copy(
 *             roundLength = 120,
 *         ),
 *     ),
 * )
 * ```
 */
sealed interface CompetitionSession {
    val id: CommonSession.Id

    fun copy(
        creatorId: User.Id? = null,
        settings: CompetitionSettings? = null,
        stage: CompetitionStage? = null,
        teams: List<CompetitionTeam>? = null,
    ): CompetitionSession

    sealed interface WithCommonFields : CompetitionSession {
        val creatorId: User.Id
        val lastModified: LocalDateTime

        companion object : CompetitionSessionFieldSelector<WithCommonFields>
    }

    sealed interface WithSettings : CompetitionSession {
        val settings: CompetitionSettings

        companion object : CompetitionSessionFieldSelector<WithSettings>
    }

    sealed interface WithStage : CompetitionSession {
        val stage: CompetitionStage

        companion object : CompetitionSessionFieldSelector<WithStage>
    }

    sealed interface WithTeams : CompetitionSession {
        val teams: List<CompetitionTeam>

        companion object : CompetitionSessionFieldSelector<WithTeams>
    }

    data class Full(
        val _id: CommonSession.Id?,
        val _creatorId: User.Id? = null,
        val _lastModified: LocalDateTime? = null,
        val _settings: CompetitionSettings? = null,
        val _stage: CompetitionStage? = null,
        val _teams: List<CompetitionTeam>? = null,
    ) : WithCommonFields,
        WithTeams,
        WithSettings,
        WithStage {
        override val id: CommonSession.Id
            get() = _id ?: error("")

        override val creatorId: User.Id
            get() = _creatorId ?: error("")

        override val lastModified: LocalDateTime
            get() = _lastModified ?: error("")

        override val settings: CompetitionSettings
            get() = _settings ?: error("")

        override val stage: CompetitionStage
            get() = _stage ?: error("")

        override val teams: List<CompetitionTeam>
            get() = _teams ?: error("")

        override fun copy(
            creatorId: User.Id?,
            settings: CompetitionSettings?,
            stage: CompetitionStage?,
            teams: List<CompetitionTeam>?,
        ): CompetitionSession = copy(
            _creatorId = creatorId,
            _settings = settings,
            _stage = stage,
            _teams = teams,
        )

        companion object : CompetitionSessionFieldSelector<Full>
    }
}

sealed interface CompetitionSessionFieldSelector<in T : CompetitionSession>

/**
 * Фабричная функция для создания модели игровой сессии
 */
@Suppress("FunctionName")
fun CompetitionSession(
    id: CommonSession.Id,
    creatorId: User.Id? = null,
    lastModified: LocalDateTime? = null,
    settings: CompetitionSettings? = null,
    stage: CompetitionStage? = null,
    teams: List<CompetitionTeam>? = null,
): CompetitionSession =
    CompetitionSession.Full(
        _id = id,
        _creatorId = creatorId,
        _lastModified = lastModified,
        _settings = settings,
        _stage = stage,
        _teams = teams,
    )
