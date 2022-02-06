package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.commongame.service.SessionPinGenerator
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.CompetitionProcessService
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionCreateTeamMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionJoinTeamMessage
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*

@Service
class CompetitionService(
    private val competitionProcessService: CompetitionProcessService,
    private val sessionPinGenerator: SessionPinGenerator,
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val userRepository: UserRepository,
) {
    suspend fun create(
        userEmail: String,
        request: CreateCompetitionRequest,
    ): CreateCompetitionResponse {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        val session = competitionSessionRepository.create(
            userId = user.id,
            settings = request.extractCompetitionSettings(),
            stage = request.state.toCompetitionStage(),
        )

        return if (session.stage == CompetitionStage.Registration) {
            val pin = sessionPinGenerator.convertSessionIdToPin(session.id)
            CreateCompetitionResponse.OpenedRegistration(pin)
        } else {
            CreateCompetitionResponse.Created
        }
    }

    suspend fun getCompetitionHistory(
        userEmail: String,
        limit: Int,
        offset: Int,
    ): List<GetCompetitionResponse> {
        val user = userRepository.findByEmail(userEmail)
        checkNotNull(user)

        return competitionSessionRepository
            .findByCreatorId(user.id.number, limit, offset)
            .map {
                it.toCompetitionHistoryResponseItem(
                    isOwned = true,
                    pin = sessionPinGenerator.convertSessionIdToPin(it.id),
                )
            }
    }

    suspend fun createTeam(
        studentEmail: String,
        request: CreateTeamRequest,
    ): CreateTeamResponse {
        if (request.teamName.length < 4) {
            return CreateTeamResponse.IncorrectName
        }

        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: $studentEmail")

        val sessionId = sessionPinGenerator
            .decodeIdFromPin(request.pin)
            ?: return CreateTeamResponse.GameNotFound(request.pin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.CreateTeam(
                    teamName = request.teamName,
                ),
            )
            CreateTeamResponse.Success
        } catch (ex: CompetitionProcessException) {
            CreateTeamResponse.ProcessError(ex.message)
        }
    }

    suspend fun joinTeam(
        studentEmail: String,
        request: JoinTeamRequest,
    ): JoinTeamResponse {
        val captain = userRepository
            .findByEmail(studentEmail)
            ?: error("No such user: studentEmail")

        val sessionId = sessionPinGenerator
            .decodeIdFromPin(request.competitionPin)
            ?: return JoinTeamResponse.GameNotFound(request.competitionPin)

        return try {
            competitionProcessService.acceptCommand(
                sessionId = sessionId,
                user = captain,
                command = CompetitionCommand.JoinTeam(
                    teamName = request.teamName,
                ),
            )
            JoinTeamResponse.Success(request.teamName)
        } catch (ex: CompetitionProcessException) {
            JoinTeamResponse.ProcessError(ex.message)
        }
    }

    fun teamJoinMessageFlow(
        userEmail: String,
        sessionPin: String,
    ): Flow<TeamUpdateNotification> = flow {
        val sessionId = sessionPinGenerator
            .decodeIdFromPin(sessionPin)
            ?: error("Incorrect pin")

        competitionProcessService
            .getAllMessagesForSession(sessionId)
            .map { it.body } // не смотрим, каким командам отправлено сообщение
            .transform { msg ->
                val notification = when (msg) {
                    is CompetitionCreateTeamMessage -> msg.toUpdateNotification()
                    is CompetitionJoinTeamMessage -> msg.toUpdateNotification()
                    else -> return@transform
                }
                emit(notification)
            }
            .collect(::emit)
    }

    suspend fun ifSessionCanBeJoined(
        sessionPin: String,
    ): Boolean {
        val sessionId = sessionPinGenerator
            .decodeIdFromPinUnsafe(sessionPin)
            ?: return false

        val session = competitionSessionRepository
            .find(sessionId)
            ?: return false

        return session.stage == CompetitionStage.Registration
    }

    /*

    @PostMapping(value = "/join_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> joinTeam(Mono<Principal> principalMono, @Valid @RequestBody JoinTeamRequest joinTeamRequest) {
        var compMono = this.competitionsRepository.findByPin(joinTeamRequest.getCompetitionPin());

        var userMono = principalMono
                .map(Principal::getName)
                .flatMap(userEmail -> {
                    log.info("POST: /api/competitions/join_team, email: {}, body: {}", userEmail, joinTeamRequest);
                    return userRepository.findOneByEmail(userEmail);
                });

        return Mono.zip(compMono, userMono).flatMap(tuple -> {
            var user = tuple.getT2();
            var competition = tuple.getT1();

            return teamJoinService.joinTeam(competition, joinTeamRequest, user);
        }).map(team -> {
            this.teamConnectionNotifyService.registerTeam(team);
            return (ResponseEntity)ResponseEntity
                    .ok(JoinTeamResponse.builder().currentTeamName(team.getName()).build());
        })
                .onErrorResume(ex -> {
                    log.info(String.format("Predicted exception: %s and %s", ex.getClass().getName(), ex.getMessage()));
                        ex.printStackTrace();
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of(ex.getMessage())));})
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of("No competition with pin: " + joinTeamRequest.getCompetitionPin())));
    }
     */
}

private fun CreateCompetitionRequest.extractCompetitionSettings() =
    CompetitionSettings(
        name = name,
        expensesFormula = expensesFormula.toCompetitionExpensesFormula(),
        demandFormula = demandFormula.toCompetitionDemandFormula(),
        maxTeamsAmount = maxTeamsAmount!!,
        maxTeamSize = maxTeamSize!!,
        roundsCount = roundsCount!!,
        roundLength = roundLength!!,
        teamLossLimit = teamLossUpperbound!!,
        instruction = instruction!!,
        showPreviousRoundResults = shouldShowStudentPreviousRoundResults!!,
        endRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered!!,
        showStudentsResultsTable = shouldShowResultTableInEnd!!,
        isAutoRoundEnding = isAutoRoundEnding!!,
        showOtherTeamsMembers = showOtherTeamsMembers!!,
    )

private fun String?.toCompetitionStage(): CompetitionStage =
    when (val processedStage = this?.trim()?.uppercase()) {
        CompetitionStage.Draft.name -> CompetitionStage.Draft
        CompetitionStage.Registration.name -> CompetitionStage.Registration
        else -> error("Unknown or unsupported stage $processedStage")
    }

private fun List<Double>.toCompetitionExpensesFormula() =
    CompetitionExpensesFormula(
        xSquareCoefficient = get(0),
        xCoefficient = get(1),
        freeCoefficient = get(2),
    )

private fun List<Double>.toCompetitionDemandFormula() =
    CompetitionDemandFormula(
        freeCoefficient = get(0),
        xCoefficient = get(1),
    )

private fun CompetitionSession.toCompetitionHistoryResponseItem(
    isOwned: Boolean,
    pin: String,
) =
    GetCompetitionResponse(
        demandFormula = listOf(
            properties.settings.demandFormula.freeCoefficient.toString(),
            properties.settings.demandFormula.xCoefficient.toString(),
        ),
        expensesFormula = listOf(
            properties.settings.expensesFormula.xSquareCoefficient.toString(),
            properties.settings.expensesFormula.xCoefficient.toString(),
            properties.settings.expensesFormula.freeCoefficient.toString(),
        ),
        instruction = properties.settings.instruction,
        isAutoRoundEnding = properties.settings.isAutoRoundEnding,
        isOwned = isOwned,
        lastUpdateTime = lastModified,
        maxTeamSize = properties.settings.maxTeamSize,
        maxTeamsAmount = properties.settings.maxTeamsAmount,
        name = properties.settings.name,
        pin = pin,
        roundLength = properties.settings.roundLength,
        roundsCount = properties.settings.roundsCount,
        shouldEndRoundBeforeAllAnswered = properties.settings.endRoundBeforeAllAnswered,
        shouldShowResultTableInEnd = properties.settings.showStudentsResultsTable,
        shouldShowStudentPreviousRoundResults = properties.settings.showPreviousRoundResults,
        showOtherTeamsMembers = properties.settings.showOtherTeamsMembers,
        state = stage.name.lowercase().replaceFirstChar(Char::uppercase),
        teamLossUpperbound = properties.settings.teamLossLimit.toDouble(),
    )

private fun CompetitionCreateTeamMessage.toUpdateNotification(): TeamUpdateNotification =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = listOf(captainEmail)
    )

private fun CompetitionJoinTeamMessage.toUpdateNotification(): TeamUpdateNotification =
    TeamUpdateNotification(
        teamName = teamName,
        idInGame = idInGame,
        teamMembers = membersEmails,
    )
