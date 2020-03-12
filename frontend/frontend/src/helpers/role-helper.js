function isTeacher() {
    return window.localStorage.getItem("roles").indexOf("TEACHER") !== -1;
}

export default isTeacher;