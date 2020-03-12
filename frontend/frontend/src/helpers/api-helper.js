import {EventSourcePolyfill} from 'event-source-polyfill';

class ApiSettings {
    static #truehost = "http://localhost:8080/";
    static #host = "http://localhost:8080/api";
    static #signinEndPoint = ApiSettings.#host + "/auth/signin";
    static #signupEndPoint = ApiSettings.#host + "/auth/signup";
    static #createCompetitionEndPoint = ApiSettings.#host + "/competitions/create";
    static #checkPinEndPoint = ApiSettings.#host + "/competitions/check_pin";
    static #createTeamEndPoint = ApiSettings.#host + "/competitions/create_team";
    static #teamCreationEvents = ApiSettings.#host + "/competitions/team_join_events/";
    static #joinTeamEndPoint = ApiSettings.#host + "/competitions/join_team";
    static #getCloneInfoEndPoint = ApiSettings.#host + "/competitions/get_clone_info/";
    static #updateCompetitionParams = ApiSettings.#host + "/competitions/update_competition/";

    static trueHost() {
        return ApiSettings.#truehost;
    }

    static host() {
        return ApiSettings.#host;
    }

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

    static createTeamEndPoint() {
        return ApiSettings.#createTeamEndPoint;
    }

    static teamCreationEvents(pin) {
        return ApiSettings.#teamCreationEvents + pin;
    }

    static joinTeamEndPoint() {
        return ApiSettings.#joinTeamEndPoint;
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

    static competitionAllResults(pin) {
        return ApiSettings.host() + "/competitions/competition_results/" + pin;
    }

    static studentCompetitionInfo(pin) {
        return ApiSettings.host() + "/competition_process/" + pin + "/student_comp_info";
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

    static verificationEndPoint(token) {
        return ApiSettings.host() + "/auth/verification/" + token;
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
            body: JSON.stringify(pin)
        });
    }

    static createTeam(team) {
        return fetch(ApiSettings.createTeamEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(team)
        });
    }

    static joinTeam(team) {
        return fetch(ApiSettings.joinTeamEndPoint(), {
            method: "POST",
            headers: this.authDefaultHeaders(),
            body: JSON.stringify(team)
        })
    }

    static teamCreationEventSource(pin) {
        return new EventSourcePolyfill(ApiSettings.teamCreationEvents(pin),
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
            method: "GET",
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

    static accountVerification(token) {
        return fetch(ApiSettings.verificationEndPoint(token), {
            method: "GET",
            headers: this.defaultHeaders()
        });
    }
}
