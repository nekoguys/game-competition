import {EventSourcePolyfill} from 'event-source-polyfill';

class ApiSettings {
    static #truehost = process.env.REACT_APP_API_HOST;
    static #host = process.env.REACT_APP_API_HOST + "/api";
    static #signinEndPoint = ApiSettings.#host + "/auth/signin";
    static #signupEndPoint = ApiSettings.#host + "/auth/signup";
    static #createCompetitionEndPoint = ApiSettings.#host + "/competitions/create";
    static #checkPinEndPoint = ApiSettings.#host + "/competitions/check_pin";
    static #getCloneInfoEndPoint = ApiSettings.#host + "/competitions/get_clone_info/";
    static #updateCompetitionParams = ApiSettings.#host + "/competitions/update_competition/";

    static trueHost() {
        return ApiSettings.#truehost;
    }

    static host() {
        return ApiSettings.#host;
    }

    // --- START TEAMS API ---

    static teamsEndPoint(pin, method) {
        return ApiSettings.host() + "/competitions/" + pin + "/teams" + method;
    }

    static getCurrentTeamEndpoint(pin) {
        return ApiSettings.teamsEndPoint(pin, "/current");
    }

    static createTeamEndPoint(pin) {
        return ApiSettings.teamsEndPoint(pin, "/create");
    }

    static teamCreationEvents(pin) {
        return ApiSettings.teamsEndPoint(pin, "/all_join_events");
    }

    static myTeamNewMembersEvents(pin) {
        return ApiSettings.teamsEndPoint(pin, "/my_team_new_members");
    }

    static joinTeamEndPoint(pin) {
        return ApiSettings.teamsEndPoint(pin, "/join");
    }

    // --- END TEAMS API ---

    // --- START USERS API ---

    static usersEndPoint(method) {
        return ApiSettings.host() + "/users" + method;
    }

    static getCurrentUserEndPoint() {
        return ApiSettings.usersEndPoint("")
    }

    static updateUserEndPoint() {
        return ApiSettings.usersEndPoint("/update")
    }

    static findUserByEmailEndPoint() {
        return ApiSettings.usersEndPoint("/find_by_email")
    }

    static findUsersByFilterEndPoint() {
        return ApiSettings.usersEndPoint("/find_by_filter")
    }

    // --- END USERS API ---

    static signinEndPoint() {
        return ApiSettings.#signinEndPoint;
    }

    static signupEndPoint() {
        return ApiSettings.#signupEndPoint;
    }

    static createCompetitionEndPoint() {
        return ApiSettings.#createCompetitionEndPoint;
    }

    static checkPinEndPoint() {
        return ApiSettings.#checkPinEndPoint;
    }

    static getCloneInfoEndPoint(pin) {
        return ApiSettings.#getCloneInfoEndPoint + pin;
    }

    static updateCompetition(pin) {
        return ApiSettings.#updateCompetitionParams + pin;
    }

    static competitionMessagesEvents(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/messages_stream";
    }

    static sendCompetitionMessageEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/send_message";
    }

    static startCompetitionEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/start_competition";
    }

    static competitionRoundsEventsStream(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/rounds_stream";
    }

    static competitionAnswersStream(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/answers_stream";
    }

    static endRoundEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/end_round";
    }

    static startRoundEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/start_round";
    }

    static competitionInfoForResultsTable(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/comp_info";
    }

    static competitionsHistory(start, amount) {
        return ApiSettings.host() + "/competitions/competitions_history/" + start + "/" + amount;
    }

    static competitionResultsStream(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/results_stream";
    }

    static competitionRoundPricesStream(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/prices_stream";
    }

    static competitionTeamBanStream(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/bans";
    }

    static competitionAllResults(pin) {
        return ApiSettings.host() + "/competitions/competition_results/" + pin;
    }

    static studentCompetitionInfo(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/student_comp_info";
    }

    static competitionInfoForTeams(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/comp_info_teams";
    }

    static submitAnswerEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/submit_answer";
    }

    static myCompetitionAnswersEvents(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/my_answers_stream";
    }

    static myResultsEvents(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/my_results_stream";
    }

    static allInOneStudentEvents(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/student_all_in_one";
    }

    static allInOneTeacherEvents(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/teacher_all_in_one";
    }
    
    static getChangeRoundLengthEndPoint(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/change_round_length";
    }

    static verificationEndPoint(token) {
        return ApiSettings.host() + "/auth/verification/" + token;
    }

    static submitStrategyEndPoint(pin) {
        return ApiSettings.#host + "/competition_process/" + pin + "/submit_strategy";
    }

    static restartGameEndPoint(pin) {
        return ApiSettings.#host + "/competition_process/" + pin + "/restart_game";
    }
}

export default class ApiHelper {

    static defaultHeaders() {
        return {
            'Content-Type': 'application/json; charset=utf-8',
            'Access-Control-Allow-Origin': ApiSettings.host() + "*",
        }
    }

    static authDefaultHeaders() {
        return {
            ...this.defaultHeaders(),
            'Authorization': "Bearer " + localStorage.getItem("accessToken")
        }
    }

    static signin(credentials) {
        return fetch(ApiSettings.signinEndPoint(), {
            method: "POST",
            headers: this.defaultHeaders(),
            body: JSON.stringify(credentials)
        })
    }

    static signup(credentials) {
        return fetch(ApiSettings.signupEndPoint(), {
            method: "POST",
            headers: this.defaultHeaders(),
            body: JSON.stringify(credentials)
        })
    }

    static createCompetition(competition) {
        return fetch(ApiSettings.createCompetitionEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(competition)
        });
    }

    static checkPin(pin) {
        return fetch(ApiSettings.checkPinEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify({pin: pin})
        });
    }

    static createTeam(pin, team) {
        return fetch(ApiSettings.createTeamEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(team)
        });
    }

    static joinTeam(pin, team) {
        return fetch(ApiSettings.joinTeamEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(team)
        })
    }

    static getCurrentTeam(pin) {
        return fetch(ApiSettings.getCurrentTeamEndpoint(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static teamCreationEventSource(pin) {
        return new EventSourcePolyfill(ApiSettings.teamCreationEvents(pin),
            {
                headers: this.authDefaultHeaders(),
                heartbeatTimeout: 1000*60*60
            });
    }

    static myTeamsNewMembersEventSource(pin) {
        return new EventSourcePolyfill(ApiSettings.myTeamNewMembersEvents(pin),
            {
                headers: this.authDefaultHeaders(),
                heartbeatTimeout: 1000*60*60
            });
    }

    static getCompetitionCloneInfo(pin) {
        return fetch(ApiSettings.getCloneInfoEndPoint(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static updateCompetition(pin, params) {
        return fetch(ApiSettings.updateCompetition(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(params)
        })
    }

    static competitionMessagesEventSource(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionMessagesEvents(pin),
            {
                headers: this.authDefaultHeaders(),
                heartbeatTimeout: 1000*60*60
            });
    }

    static sendCompetitionMessage(pin, message) {
        return fetch(ApiSettings.sendCompetitionMessageEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify({message: message})
        });
    }

    static startCompetition(pin) {
        return fetch(ApiSettings.startCompetitionEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
        })
    }

    static competitionRoundEventsStream(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionRoundsEventsStream(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static competitionAnswersStream(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionAnswersStream(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static startNewCompetitionRound(pin) {
        return fetch(ApiSettings.startRoundEndPoint(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static endCompetitionRound(pin) {
        return fetch(ApiSettings.endRoundEndPoint(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static competitionInfoForResultsTable(pin) {
        return fetch(ApiSettings.competitionInfoForResultsTable(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static competitionsHistory(start, amount) {
        return fetch(ApiSettings.competitionsHistory(start, amount), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static competitionResultsStream(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionResultsStream(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static competitionRoundPricesStream(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionRoundPricesStream(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static competitionTeamBanStream(pin) {
        return new EventSourcePolyfill(ApiSettings.competitionTeamBanStream(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static competitionAllResults(pin) {
        return fetch(ApiSettings.competitionAllResults(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static studentCompetitionInfo(pin) {
        return fetch(ApiSettings.studentCompetitionInfo(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static competitionInfoForTeams(pin) {
        return fetch(ApiSettings.competitionInfoForTeams(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static submitAnswer(pin, obj) {
        return fetch(ApiSettings.submitAnswerEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(obj)
        });
    }

    static myAnswersStream(pin) {
        return new EventSourcePolyfill(ApiSettings.myCompetitionAnswersEvents(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static myResultsStream(pin) {
        return new EventSourcePolyfill(ApiSettings.myResultsEvents(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static allInOneStudentStream(pin) {
        return new EventSourcePolyfill(ApiSettings.allInOneStudentEvents(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static allInOneTeacherStream(pin) {
        return new EventSourcePolyfill(ApiSettings.allInOneTeacherEvents(pin), {
            headers: this.authDefaultHeaders(),
            heartbeatTimeout: 1000*60*60
        })
    }

    static accountVerification(token) {
        return fetch(ApiSettings.verificationEndPoint(token), {
            method: "GET",
            headers: this.defaultHeaders()
        });
    }

    static updateProfile(params) {
        return fetch(ApiSettings.updateUserEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(params)
        })
    }

    static getProfile() {
        return fetch(ApiSettings.getCurrentUserEndPoint(), {
            method: "GET",
            headers: this.authDefaultHeaders(),
        })
    }

    static getNavBarInfo() {
        return fetch(ApiSettings.getCurrentUserEndPoint(), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }

    static changeRole(email, role) {
        return fetch(ApiSettings.updateUserEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify({email, role})
        })
    };

    static changeRoundLength(pin, roundLength) {
        return fetch(ApiSettings.getChangeRoundLengthEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify({newRoundLength: roundLength})
        });
    }

    static adminkaSearchUsers(params) {
        return fetch(ApiSettings.findUsersByFilterEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(params)
        })
    }

    static adminkaChangePassword(params) {
        return fetch(ApiSettings.updateUserEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(params)
        })
    }

    static submitStrategy(params, pin) {
        return fetch(ApiSettings.submitStrategyEndPoint(pin), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(params)
        })
    }

    static restartGame(pin) {
        return fetch(ApiSettings.restartGameEndPoint(pin), {
            method: "GET",
            headers: this.authDefaultHeaders()
        })
    }
}
