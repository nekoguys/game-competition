class CancellablePromise {
    constructor(promise) {
        this.hasCanceled = false;
        this.promise = new Promise((resolve, reject) => {
            promise.then(
                val => this.hasCanceled ? reject({isCanceled: true}) : resolve(val),
                error => this.hasCanceled ? reject({isCanceled: true}) : reject(error)
            );
        });
    }

    cancel() {
        this.hasCanceled = true;
    }
}

const makeCancelable = (promise) => {
    let hasCanceled_ = false;

    const wrappedPromise = new Promise((resolve, reject) => {
        promise.then(
            val => hasCanceled_ ? reject({isCanceled: true}) : resolve(val),
            error => hasCanceled_ ? reject({isCanceled: true}) : reject(error)
        );
    });

    return new CancellablePromise(promise);
};

export default makeCancelable;
