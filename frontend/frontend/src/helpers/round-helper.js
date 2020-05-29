function round(value) {
    if (typeof value == "undefined") {
        return ""
    }
    return value.toFixed(2);
}

export default round;
