const getUTCSeconds = () => {
    const date = new Date();
    const UTCseconds = (date.getTime() + date.getTimezoneOffset()*60*1000)/1000;

    return UTCseconds;
};

const isAuthenticated = () => {
    const val = localStorage.getItem("expirationTimestamp");
    console.log({val});

    if (val == null || (parseInt(val) <= getUTCSeconds())) {
        console.log({isAuthenticated: false, val: val, utc: getUTCSeconds()});
        return false;
    }
    console.log({isAuthenticated: true});
    return true;
};

export default isAuthenticated;
