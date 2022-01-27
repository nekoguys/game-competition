package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("competition_game_states")
data class DbCompetitionState(
    @Id
    var id: Long? = null,
    @Column("name")
    var state: State
) {
    enum class State {
        DRAFT, REGISTRATION, IN_PROCESS, ENDED
    }
}
