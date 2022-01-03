const fetchPromise = (params, promiseProvider) => {
    return new Promise((resolve, reject) => {
        promiseProvider(params).then((response) => {
            if (response.status >= 300) {
                response.text().then((text) => {
                    reject(new Error("Not 200 status code: " + text))
                })
            } else {
                response.json().then((jsonValue) => resolve(jsonValue))
            }
        }, (error) => {
            reject(error);
        })
    })
};

export default fetchPromise;