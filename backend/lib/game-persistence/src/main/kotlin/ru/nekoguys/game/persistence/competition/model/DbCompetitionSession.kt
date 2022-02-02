package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nekoguys.game.entity.competition.model.CompetitionStage

@Table("competition_game_sessions")
data class DbCompetitionSession(
    @Id
    @Column("id")
    var parentId: Long?,

    var stage: DbCompetitionStage,

    var lastRound: Int?,
) : Persistable<Long> {

    @Transient
    private var isNew: Boolean = parentId == null

    fun asNew(): DbCompetitionSession =
        apply { isNew = true }

    override fun isNew(): Boolean = isNew

    override fun getId(): Long? = parentId
}

enum class DbCompetitionStage {
    DRAFT,
    REGISTRATION,
    IN_PROGRESS,
    ENDED,
    UNKNOWN,
}

fun CompetitionStage.toDbCompetitionStage() =
    when (this) {
        is CompetitionStage.Draft -> DbCompetitionStage.DRAFT
        is CompetitionStage.Registration -> DbCompetitionStage.REGISTRATION
        is CompetitionStage.InProgress -> DbCompetitionStage.IN_PROGRESS
        is CompetitionStage.Ended -> DbCompetitionStage.ENDED
    }

fun CompetitionStage.extractLastRound(): Int? =
    if (this is CompetitionStage.InProgress) {
        round
    } else {
        null
    }

fun DbCompetitionSession.extractCompetitionStage() =
    when (stage) {
        DbCompetitionStage.DRAFT -> CompetitionStage.Draft
        DbCompetitionStage.REGISTRATION -> CompetitionStage.Registration
        DbCompetitionStage.IN_PROGRESS -> CompetitionStage.InProgress(lastRound!!)
        DbCompetitionStage.ENDED -> CompetitionStage.Ended
        DbCompetitionStage.UNKNOWN -> error("Got unknown competition stage")
    }
