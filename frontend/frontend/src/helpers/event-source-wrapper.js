class EventSourceWrapper {
    constructor(eventSource) {
        this.eventSource = eventSource
    }

    subscribe(callback) {
        const wrappedCallback = (event) => {
            let data = JSON.parse(event.data)
            callback({...data, lastEventId: event.lastEventId})
        };
        this.eventSource.addEventListener("message", wrappedCallback);
    }

    close() {
        this.eventSource.close()
    }
}

export default EventSourceWrapper;
