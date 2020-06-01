import isAuthenticated from "./is-authenticated";

function isTeacher() {
    const roles = window.localStorage.getItem("roles");
    if (roles == null) {
        return false;
    }
    return roles.indexOf("TEACHER") !== -1 && isAuthenticated();
}

export default isTeacher;