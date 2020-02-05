import {EventSourcePolyfill} from 'event-source-polyfill';

class ApiSettings {
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
                //heartbeatTimeout: 1000*60*60
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
}
