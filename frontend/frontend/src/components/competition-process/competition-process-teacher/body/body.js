import React from "react";

import "./body.css"
import DefaultSubmitButton from "../../../common/default-submit-button";
import CompetitionResultsTable from "../results-table";
import MessagesContainer from "../messages";
import ApiHelper from "../../../../helpers/api-helper";
import {NotificationContainer, NotificationManager} from "react-notifications";
import processRoundsEvents from "../../../../helpers/rounds-event-source-helper";
import processMessagesEvents from "../../../../helpers/messages-event-source-helper";


class CompetitionProcessTeacherBody extends React.Component {
    constructor(props) {
        super(props);

        this.eventsSource = undefined;

        this.state = {
            currentRoundNumber: 0,
            timeTillRoundEnd: 0,
            isCurrentRoundEnded: false,
            name: "",
            teamsCount: 10,
            roundsCount: 0,
            answers: {},
            results: {},
            prices: {},
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
            teamsCount={this.state.teamsCount}
            roundsCount={this.state.roundsCount}
            results={this.state.results}
            prices={this.state.prices}
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
        this.getCompetitionInfo();
        this.setupResultsEvents();
        this.setupPricesEvents();
    }

    componentWillUnmount() {
        this.closeCompetitionMessagesEvents();
        this.closeRoundEvents();
        this.closeAnswersEvents();
        this.closeCompetitionResultsEvents();
        this.closePricesEvents();
    }

    getCompetitionInfo() {
        const {pin} = this.props;

        ApiHelper.competitionInfoForResultsTable(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()};
            }

            return {success: true, json: resp.json()};
        }).then(resp => {
            resp.json.then(jsonBody => {
                if (resp.success) {
                    this.setState(prevState => {
                        this.props.updateCompetitionNameCallback(jsonBody.name);

                        if (jsonBody.roundsCount === prevState.currentRoundNumber && jsonBody.roundsCount !== 0) {
                            this.props.onEndCallback();
                        }

                        return {
                            name: jsonBody.name,
                            teamsCount: jsonBody.connectedTeamsCount,
                            roundsCount: jsonBody.roundsCount
                        }
                    })
                } else {
                    NotificationManager.error(jsonBody.message, "Error", 1500);
                }
            })
        })
    }

    setupPricesEvents() {
        const {pin} = this.props;

        this.pricesEventSource = ApiHelper.competitionRoundPricesStream(pin);

        this.pricesEventSource.addEventListener("message", (message) => {
            const {price, roundNumber} = JSON.parse(message.data);

            this.setState(prevState => {
                const prices = {...prevState.prices};

                prices[roundNumber] = price;
                return {prices};
            })
        })
    }

    setupResultsEvents() {
        const {pin} = this.props;

        this.resultsEventSource = ApiHelper.competitionResultsStream(pin);

        this.resultsEventSource.addEventListener("error", (err) => {
            console.log("resultsEventSource error: ");
            console.log({err});
        });

        this.resultsEventSource.addEventListener("message", (message) => {
            const resultsData = JSON.parse(message.data);
            console.log({resultsData});
            const teamIdInGame = resultsData.teamIdInGame;
            const round = resultsData.roundNumber;
            const income = resultsData.income;

            this.setState(prevState => {
                const results = {...prevState.results};

                if (!(round in results)) {
                    results[round] = {[teamIdInGame]: income};
                } else {
                    results[round][teamIdInGame] = income;
                }

                return {results: results};
            })
        })
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
            this.setState((prevState) => {
                return processRoundsEvents(message);
            });
        });
    }

    setupCompetitionMessagesEvents() {
        const {pin} = this.props;

        this.eventsSource = ApiHelper.competitionMessagesEventSource(pin);
        this.eventsSource.addEventListener("error", (err) => console.log("EventSource failed: " + err));

        this.eventsSource.addEventListener("message", event => {
            this.setState(prevState => {
                return processMessagesEvents(event, [...prevState.messages]);
            });
        })
    }

    closeCompetitionResultsEvents() {
        if (this.resultsEventSource !== undefined) {
            this.resultsEventSource.close();
        }
    }

    closeCompetitionMessagesEvents() {
        if (this.eventSource !== undefined)
            this.eventSource.close();
    }

    closePricesEvents() {
        if (this.pricesEventSource !== undefined) {
            this.pricesEventSource.close();
        }
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
        console.log("click on start/end round button");
        const {pin} = this.props;
        let pr;
        if (this.state.isCurrentRoundEnded) {
            pr = ApiHelper.startNewCompetitionRound(pin);
            console.log("start");
        } else {
            pr = ApiHelper.endCompetitionRound(pin);
            console.log("end");
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
                    <CompetitionResultsTable style={{width: "100%"}} teamsCount={this.props.teamsCount}
                                             roundsCount={this.props.roundsCount}
                                             answers={this.props.answers}
                                             results={this.props.results}
                                             prices={this.props.prices}
                    />
                </div>
                <div style={{paddingTop: "20px"}}>
                    <MessagesContainer messages={this.props.messages} sendMessageCallBack={this.props.sendMessageCallBack}/>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessTeacherBody;
