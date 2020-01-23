class ApiSettings {
    static #host = "http://localhost:8080/api";
    static #signinEndPoint = ApiSettings.#host + "/auth/signin";
    static #signupEndPoint = ApiSettings.#host + "/auth/signup";
    static #createCompetitionEndPoint = ApiSettings.#host + "/competitions/create";
    static #checkPinEndPoint = ApiSettings.#host + "/competitions/check_pin";

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
}
