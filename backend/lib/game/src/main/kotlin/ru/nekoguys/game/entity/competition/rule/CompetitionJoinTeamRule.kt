package ru.nekoguys.game.entity.competition.rule

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.students
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.user.repository.UserRepository

data class CompetitionJoinTeamMessage(
    val teamName: String,
    val idInGame: Int,
    val membersEmails: List<String>,
) : CompetitionMessage()

@Component
class CompetitionJoinTeamRule(
    private val userRepository: UserRepository,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) : CompetitionRule<CompetitionPlayer.Unknown, CompetitionCommand.JoinTeam, CompetitionJoinTeamMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.JoinTeam,
    ): List<CompGameMessage<CompetitionJoinTeamMessage>> {
        val session = competitionSessionRepository
            .load(
                player.sessionId,
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeams,
            )

        val team = session
            .teams
            .singleOrNull { it.name == command.teamName }
            ?: throw CompetitionProcessException(
                "No team in competition with name: ${command.teamName}"
            )

        val newStudent = CompetitionPlayer.TeamMember(
            sessionId = player.sessionId,
            user = player.user,
            teamId = team.id,
        )
        val newTeam = team.copy(
            teamMembers = team.teamMembers + newStudent
        )
        competitionTeamRepository.update(from = team, to = newTeam)

        val memberEmails = userRepository
            .findAll(newTeam.students.map { it.user.id.number })
            .toList()
            .map { it.email }

        return buildResponse {
            team.id {
                +CompetitionJoinTeamMessage(
                    teamName = command.teamName,
                    idInGame = team.numberInGame,
                    membersEmails = memberEmails,
                )
            }
        }
    }
}
