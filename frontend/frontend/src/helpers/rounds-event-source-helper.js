export default function processRoundsEvents(roundMessage) {
    console.log({competitionRoundEventSourceData: roundMessage.data});
    const messageData = JSON.parse(roundMessage.data);
    console.log({messageData});


    if (messageData.type.toLowerCase() === 'newround') {
        console.log({tmstmp : new Date().getTime()});
        const timeTillRoundEnd = messageData.roundLength - (Math.round((new Date().getTime())/1000) - messageData.beginTime);
        return {currentRoundNumber: messageData.roundNumber, timeTillRoundEnd: timeTillRoundEnd, isCurrentRoundEnded: false};
    } else {
        return {isCurrentRoundEnded: true, currentRoundNumber: messageData.roundNumber};
    }
}