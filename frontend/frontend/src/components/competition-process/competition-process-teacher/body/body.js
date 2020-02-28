import React from "react";

import "./body.css"
import DefaultSubmitButton from "../../../common/default-submit-button";
import CompetitionResultsTable from "../results-table";
import MessagesContainer from "../messages";
import ApiHelper from "../../../../helpers/api-helper";
import {NotificationContainer, NotificationManager} from "react-notifications";


class CompetitionProcessTeacherBody extends React.Component {
    constructor(props) {
        super(props);

        this.eventsSource = undefined;

        this.state = {
            currentRoundNumber: 0,
            timeTillRoundEnd: 0,
            isCurrentRoundEnded: false,
            answers: {},
            messages: []
        }
    }

    render() {
        let res = (<CompetitionProcessTeacherActive
            round={this.state.currentRoundNumber}
            timeLeft={this.state.timeTillRoundEnd}
            isRoundEnded={this.state.isCurrentRoundEnded}
            messages={this.state.messages}
            answers={this.state.answers}
            sendMessageCallBack={this.sendMessageCallback}
            rightButtonClick={this.onStartOrEndRoundButtonClick}
        />);

        return (
            <div>
            <div className={"game-state-holder"}>
                {res}
            </div>
                <div>
                    <NotificationContainer/>
                </div>
            </div>
        );
    }

    componentDidMount() {
        this.setupCompetitionMessagesEvents();
        this.setupRoundsEvents();
        this.setupAnswerEvents();
    }

    componentWillUnmount() {
        this.closeCompetitionMessagesEvents();
        this.closeRoundEvents();
        this.closeAnswersEvents();
    }

    setupAnswerEvents() {
        const {pin} = this.props;

        this.answersEventSource = ApiHelper.competitionAnswersStream(pin);

        this.answersEventSource.addEventListener("error", (err) => {
            console.log("answersEventSource error: " + err);
        });

        this.answersEventSource.addEventListener("message", (message) => {
            const answerData = JSON.parse(message.data);
            const teamIdInGame = answerData.teamIdInGame;
            const round = answerData.roundNumber;
            const answer = answerData.teamAnswer;

            this.setState(prevState => {
                const answers = {...prevState.answers};

                if (!(round in answers)) {
                    answers[round] = {[teamIdInGame]: answer}
                } else {
                    answers[round][teamIdInGame] = answer;
                }

                return {answers: answers};
            })
        });
    }

    setupRoundsEvents() {
        const {pin} = this.props;

        this.competitionRoundEventSource = ApiHelper.competitionRoundEventsStream(pin);
        this.competitionRoundEventSource.addEventListener("error",
            (err) => console.log("competitionRoundEventSource failed: " + err))

        this.competitionRoundEventSource.addEventListener("message", (message) => {
            console.log({competitionRoundEventSourceData: message.data});
            const messageData = JSON.parse(message.data);
            console.log({messageData});

            this.setState((prevState) => {
                if (messageData.type.toLowerCase() === 'newround') {
                    console.log({tmstmp : new Date().getTime()});
                    const timeTillRoundEnd = messageData.roundLength - (Math.round((new Date().getTime())/1000) - messageData.beginTime);
                    return {currentRoundNumber: messageData.roundNumber, timeTillRoundEnd: timeTillRoundEnd, isCurrentRoundEnded: false};
                } else {
                    return {isCurrentRoundEnded: true};
                }
            });
        });
    }

    setupCompetitionMessagesEvents() {
        const {pin} = this.props;

        this.eventsSource = ApiHelper.competitionMessagesEventSource(pin);
        this.eventsSource.addEventListener("error", (err) => console.log("EventSource failed: " + err));

        this.eventsSource.addEventListener("message", event => {
            console.log(event.data);
            this.setState((prevState) => {
                const prevMessages = prevState.messages;
                let arr = prevMessages.slice(0);
                const elem = JSON.parse(event.data);
                const date = new Date(elem.sendTime * 1000);

                const dateStr = date.toLocaleDateString("en-US", {
                    hour: 'numeric',
                    minute: 'numeric',
                    day: 'numeric',
                    month: 'short',
                });

                const messageElem = {
                    message: elem.message,
                    dateStr: dateStr,
                    timestamp: elem.sendTime
                };

                const index = arr.findIndex(el => {
                    return el.message === messageElem.message && el.timestamp === messageElem.timestamp;
                });

                if (index == -1) {
                    arr = [messageElem].concat(arr);
                } else {
                    arr[index] = messageElem;
                }

                return {messages: arr};
            })
        })
    }

    closeCompetitionMessagesEvents() {
        if (this.eventSource !== undefined)
            this.eventSource.close();
    }

    closeAnswersEvents() {
        if (this.answersEventSource !== undefined) {
            this.answersEventSource.close();
        }
    }

    closeRoundEvents() {
        if (this.competitionRoundEventSource !== undefined) {
            this.competitionRoundEventSource.close();
        }
    }

    onStartOrEndRoundButtonClick = () => {
        const {pin} = this.props;
        let pr;
        if (this.state.isCurrentRoundEnded) {
            pr = ApiHelper.startNewCompetitionRound(pin);
        } else {
            pr = ApiHelper.endCompetitionRound(pin);
        }

        pr.then(resp => {
            console.log({resp});
            if (resp.status >= 300) {
                return {success: false, json: resp.text()};
            }

            return {success: true, json: resp.json()};
        }).then(resp => {
            resp.json.then(jsonBody => {
                console.log({jsonBody});
                if (resp.success) {
                    NotificationManager.success(jsonBody.message, "Success", 900);
                } else {
                    NotificationManager.error(jsonBody.message, "Error", 1500);
                }
            })
        })
    };

    sendMessageCallback = (message) => {
        ApiHelper.sendCompetitionMessage(this.props.pin, message).then(resp => {
            console.log({resp});
            if (resp.status >= 300) {
                return {success: false, json: resp.text()};
            }

            return {success: true, json: resp.json()};
        }).then(resp => {
            resp.json.then(jsonBody => {
                console.log({jsonBody});
                if (resp.success) {
                    NotificationManager.success(jsonBody.message, "Success", 900);
                } else {
                    NotificationManager.error(jsonBody.message, "Error", 1500);
                }
            })
        })
    }
}

class CompetitionProcessTeacherActive extends React.Component {
    render() {
        const {round, timeLeft, isRoundEnded} = this.props;

        const rightButtonText = isRoundEnded ? "Начать новый раунд" : "Закончить раунд";

        return (
            <div>
                <div className={"row justify-content-between"}>
                    <div className={"col-4"}>
                        <div>
                            <div style={{textAlign: "center", fontSize: "23px"}}>
                                {"Текущий раунд: " + round}
                            </div>
                            <div style={{paddingTop: "20px"}}>
                                <DefaultSubmitButton text={"Начать раунд заново"}/>
                            </div>
                        </div>
                    </div>
                    <div className={"col-4"}>
                        <div style={{textAlign: "center", fontSize: "23px"}}>
                            {"До конца раунда: " + timeLeft + "сек"}
                        </div>
                        <div style={{paddingTop: "20px"}}>
                            <DefaultSubmitButton text={rightButtonText} onClick={this.props.rightButtonClick}/>
                        </div>
                    </div>
                </div>
                <div style={{paddingTop: "20px"}}>
                    <CompetitionResultsTable style={{width: "100%"}} teamsCount={10} roundsCount={6} answers={this.props.answers} />
                </div>
                <div style={{paddingTop: "20px"}}>
                    <MessagesContainer messages={this.props.messages} sendMessageCallBack={this.props.sendMessageCallBack}/>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessTeacherBody;
