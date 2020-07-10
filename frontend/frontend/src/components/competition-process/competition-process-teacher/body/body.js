import React from "react";

import "./body.css"
import DefaultSubmitButton from "../../../common/default-submit-button";
import CompetitionResultsTable from "../results-table";
import MessagesContainer from "../messages";
import ApiHelper from "../../../../helpers/api-helper";
import processRoundsEvents from "../../../../helpers/rounds-event-source-helper";
import processMessagesEvents from "../../../../helpers/messages-event-source-helper";
import showNotification from "../../../../helpers/notification-helper";
import * as Constants from "../../../../helpers/constants";
import ChangeRoundLengthContainer from "../change-round-length-container";


class CompetitionProcessTeacherBody extends React.Component {
    constructor(props) {
        super(props);

        this.eventsSource = undefined;

        this.state = {
            currentRoundNumber: 0,
            timeTillRoundEnd: 0,
            isCurrentRoundEnded: false,
            currentRoundLength: 0,
            name: "",
            teamsCount: 10,
            roundsCount: 0,
            isAutoRoundEnding: false,
            answers: {},
            results: {},
            prices: {},
            messages: [],
            bannedTeams: []
        };
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
            bannedTeams={this.state.bannedTeams}
            currentRoundLength={this.state.currentRoundLength}
            changeRoundLengthCallback={this.changeRoundLength}
            isAutoRoundEnding={this.state.isAutoRoundEnding}
        />);

        return (
            <div>
            <div className={"game-state-holder"}>
                {res}
            </div>
            </div>
        );
    }

    componentDidMount() {
        this.getCompetitionInfo();
        this.setupAllInOneEvents();

        this.setupTimer();
    }

    componentWillUnmount() {
        if (this.eventSource !== undefined) {
            this.eventSource.close();
        }

        clearInterval(this.timerId);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.state.isCurrentRoundEnded) {
            clearInterval(this.timerId);
        } else if (!this.state.isCurrentRoundEnded && prevState.isCurrentRoundEnded) {
            clearInterval(this.timerId);
            this.setupTimer();
        }

        if (this.state.roundsCount > 0 && this.state.roundsCount === this.state.currentRoundNumber && this.state.isCurrentRoundEnded) {
            this.props.onEndCallback();
        }
    }

    setupTimer = () => {
        this.timerId = setInterval(() => {
            this.setState(prevState => {
                return {timeTillRoundEnd: Math.max(prevState.timeTillRoundEnd - 1, 0)};
            })
        }, 1000);
    };

    getCompetitionInfo() {
        console.log("getCompetitionInfo call");
        const {pin} = this.props;

        ApiHelper.competitionInfoForResultsTable(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()};
            }

            return {success: true, json: resp.json()};
        }).then(resp => {
            resp.json.then(jsonBody => {
                console.log({competitionInfoCallData: jsonBody});
                if (resp.success) {
                    this.setState(prevState => {
                        this.props.updateCompetitionNameCallback(jsonBody.name);

                        if (jsonBody.roundsCount === prevState.currentRoundNumber && jsonBody.roundsCount !== 0) {
                            this.props.onEndCallback();
                        }

                        return {
                            name: jsonBody.name,
                            teamsCount: jsonBody.connectedTeamsCount,
                            roundsCount: jsonBody.roundsCount,
                            isAutoRoundEnding: jsonBody.isAutoRoundEnding
                        }
                    })
                } else {
                    showNotification(this).error(jsonBody.message, "Error", 1500);
                }
            })
        })
    }

    changeRoundLength = (newRoundLength) => {
        const ans = parseInt(newRoundLength, 10);
        if (Number.isNaN(ans)) {
            showNotification(this).error("Invalid round length", "Error", 2500);
        } else {
            const {pin} = this.props;

            ApiHelper.changeRoundLength(pin, ans).then(resp => {
                if (resp.status >= 300) {
                    return {success: false, json: resp.text()};
                }

                return {success: true, json: resp.json()};
            }).then(resp => {
                resp.json.then(jsonBody => {
                    console.log({changeRoundLengthResponse: jsonBody});
                    if (resp.success) {
                        showNotification(this).success(jsonBody.message, "Success", 2500);
                    } else {
                        showNotification(this).error(jsonBody.message, "Error", 2500);
                    }
                })
            })
        }
    }

    setupAllInOneEvents() {
        const {pin} = this.props;

        this.eventSource = ApiHelper.allInOneTeacherStream(pin);

        this.eventSource.addEventListener("error", (err) => console.log({eventSource: err}));

        this.eventSource.addEventListener("message", (message) => {
            if (message.lastEventId === Constants.ANSWER_EVENT_ID) {
                this.processAnswer(JSON.parse(message.data));
            } else if (message.lastEventId === Constants.MESSAGE_EVENT_ID) {
                this.processMessage(message);
            } else if (message.lastEventId === Constants.PRICE_EVENT_ID) {
                this.processPrice(JSON.parse(message.data));
            } else if (message.lastEventId === Constants.RESULT_EVENT_ID) {
                this.processResult(JSON.parse(message.data));
            } else if (message.lastEventId === Constants.ROUND_EVENT_ID) {
                this.processRound(message)
            } else if (message.lastEventId === Constants.BAN_EVENT_ID) {
                this.processBan(JSON.parse(message.data));
            }
        })

    }

    processBan = (obj) => {
        this.setState(prevState => {
            const bannedTeams = [...prevState.bannedTeams];
            bannedTeams.push(obj.teamIdInGame);
            return {bannedTeams};
        });

        showNotification(this).warning(`Team ${obj.teamIdInGame} "${obj.teamName}" was banned`, 'BAN', 3000);
    }

    processPrice = (obj) => {
        this.setState(prevState => {
            const prices = {...prevState.prices};

            prices[obj.roundNumber] = obj.price;
            return {prices};
        })
    }

    processResult = (data) => {
        const teamIdInGame = data.teamIdInGame;
        const round = data.roundNumber;
        const income = data.income;

        this.setState(prevState => {
            const results = {...prevState.results};

            if (!(round in results)) {
                results[round] = {[teamIdInGame]: income};
            } else {
                results[round][teamIdInGame] = income;
            }

            return {results: results};
        })
    }

    processAnswer = (answerData) => {
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
    }

    processRound = (message) => {
        this.setState((prevState) => {
            return processRoundsEvents(message);
        });
    }

    processMessage = (event) => {
        this.setState(prevState => {
            return processMessagesEvents(event, [...prevState.messages]);
        });
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
                    showNotification(this).success(jsonBody.message, "Success", 900);
                } else {
                    showNotification(this).error(jsonBody.message, "Error", 1500);
                }
            })
        })
    };

    sendMessageCallback = (message) => {
        console.log({sendMessage: message});
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
                    showNotification(this).success(jsonBody.message, "Success", 900);
                } else {
                    showNotification(this).error(jsonBody.message, "Error", 1500);
                }
            })
        })
    }
}

class CompetitionProcessTeacherActive extends React.Component {
    render() {
        const {round, timeLeft, isRoundEnded} = this.props;


        const rightButtonText = round === 0 ? "Начать игру" : (isRoundEnded ? "Начать новый раунд" : "Закончить раунд");

        const roundText = round === 0 ? "Игра еще не началась" : ("Текущий раунд: " + round.toString() + (isRoundEnded ? " закончен" : ""));

        console.log({props: this.props});

        let beginEndRoundButton;
        if (!this.props.isAutoRoundEnding || this.props.round === 0) {
            beginEndRoundButton = (
                <div style={{paddingTop: "20px"}}>
                    <DefaultSubmitButton text={rightButtonText} onClick={this.props.rightButtonClick}/>
                </div>
            )
        }

        return (
            <div>
                <div className={"row justify-content-between"}>
                    <div className={"col-4"}>
                        <div>
                            <div style={{textAlign: "center", fontSize: "23px"}}>
                                {roundText}
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
                        {beginEndRoundButton}
                    </div>
                </div>
                <div style={{paddingTop: "20px"}}>
                    <CompetitionResultsTable style={{width: "100%"}} teamsCount={this.props.teamsCount}
                                             roundsCount={this.props.roundsCount}
                                             answers={this.props.answers}
                                             results={this.props.results}
                                             prices={this.props.prices}
                                             bannedTeams={this.props.bannedTeams}
                    />
                </div>
                <div style={{paddingTop: "20px"}}>
                    <ChangeRoundLengthContainer currentRoundLength={this.props.currentRoundLength} changeRoundLengthCallback={this.props.changeRoundLengthCallback}/>
                </div>
                <div style={{paddingTop: "40px"}}>
                    <MessagesContainer messages={this.props.messages} sendMessageCallBack={this.props.sendMessageCallBack}/>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessTeacherBody;
