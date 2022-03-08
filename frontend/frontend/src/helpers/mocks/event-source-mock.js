class EventSourceMock {
    constructor(items, delay) {
        this.items = items
        this.delay = delay
    }

    subscribe(callback) {
        console.log("EventSourceMock opened")
        let delay = this.delay;
        for (const item of this.items) {
            setTimeout(() => { callback(item) }, delay);
            delay += this.delay;
        }
    }

    close() {
        console.log("EventSourceMock closed")
    }
}

export default EventSourceMock;