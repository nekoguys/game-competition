class ApiSettings {
    static #host = "http://localhost:8080/api";
    static #signinEndPoint = ApiSettings.#host + "/auth/signin";
    static #signupEndPoint = ApiSettings.#host + "/auth/signup";

    static host() {
        return ApiSettings.#host;
    }

    static signinEndPoint() {
        return ApiSettings.#signinEndPoint;
    }

    static signupEndPoint() {
        return ApiSettings.#signupEndPoint;
    }
}

export default class ApiHelper {
    static signin(credentials) {
        return fetch(ApiSettings.signinEndPoint(), {
            method: "POST",
            //mode: 'no-cors',
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
                'Access-Control-Allow-Origin': ApiSettings.host() + "*",
            },
            body: JSON.stringify(credentials)
        })
    }

    static signup(credentials) {
        return fetch(ApiSettings.signupEndPoint(), {
            method: "POST",
            //mode: 'no-cors',
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
                'Access-Control-Allow-Origin': ApiSettings.host() + "*",
            },
            body: JSON.stringify(credentials)
        })
    }
}
