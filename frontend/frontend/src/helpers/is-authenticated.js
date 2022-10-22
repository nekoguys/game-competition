export const getUTCSeconds = (date) => {
    console.log({date});
    return (date.getTime() + date.getTimezoneOffset() * 60 * 1000);
};

const isAuthenticated = () => {
    const val = localStorage.getItem("expirationTimestamp");
    console.log({val});
    if (Number.isNaN(parseInt(val)) || (parseInt(val) <= getUTCSeconds(new Date()))) {
        console.log({isAuthenticated: false, val: val, utc: getUTCSeconds(new Date())});
        return false;
    }
    console.log({isAuthenticated: true});
    return true;
};

export default isAuthenticated;
