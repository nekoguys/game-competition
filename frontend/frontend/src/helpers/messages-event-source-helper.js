export default function processMessagesEvents(newMessage, messages) {
    const data = JSON.parse(newMessage.data);
    return processMessageParsedEvent(data, messages);
}

export function parseMessage(message) {
    const date = new Date(message.sendTime * 1000);
    const dateStr = date.toLocaleDateString("en-US", {
        hour: 'numeric',
        minute: 'numeric',
        day: 'numeric',
        month: 'short',
    });

    return {
        message: message.message,
        dateStr: dateStr,
        timestamp: message.sendTime
    };
}

export function processMessageParsedEvent(newMessage, messages) {
    const messageElem = parseMessage(newMessage);

    const index = messages.findIndex(el => {
        return el.message === messageElem.message && el.timestamp === messageElem.timestamp;
    });

    if (index === -1) {
        messages = [messageElem].concat(messages);
    } else {
        messages[index] = messageElem;
    }

    return {messages: messages};
}