class EventSourceWrapper {
    constructor(eventSource) {
        this.eventSource = eventSource
    }

    subscribe(callback) {
        this.eventSource.addEventListener("message", callback);
    }

    close() {
        this.eventSource.close()
    }
}

export default EventSourceWrapper;
