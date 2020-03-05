export default function processMessagesEvents(newMessage, messages) {
    const data = JSON.parse(newMessage.data);
    const date = new Date(data.sendTime * 1000);
    const dateStr = date.toLocaleDateString("en-US", {
        hour: 'numeric',
        minute: 'numeric',
        day: 'numeric',
        month: 'short',
    });

    const messageElem = {
        message: data.message,
        dateStr: dateStr,
        timestamp: data.sendTime
    };

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