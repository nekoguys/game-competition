import isAuthenticated from "./is-authenticated";

export function isTeacher() {
    return check("TEACHER");
}

export function isAdmin() {
    return check("ADMIN");
}

function check(role) {
    const roles = window.localStorage.getItem("roles");
    if (roles == null) {
        return false;
    }
    return roles.indexOf(role) !== -1 && isAuthenticated();
}
