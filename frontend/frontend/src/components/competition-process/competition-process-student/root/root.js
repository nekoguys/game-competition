import React from "react";

import "./root.css";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import StudentResultsTable from "../results-table";
import SendAnswer from "../send-answer";
import MessagesContainer from "../messages";
import DescriptionHolder from "../description";
import ApiHelper from "../../../../helpers/api-helper";
import processMessagesEvents from "../../../../helpers/messages-event-source-helper";
import processRoundsEvents from "../../../../helpers/rounds-event-source-helper";
import OneRoundResultsTable from "../one-round-results-table";
import showNotification from "../../../../helpers/notification-helper";
import withAuthenticated from "../../../../helpers/with-authenticated";

import * as Constants from "../../../../helpers/constants";
import StrategySubmissionComponent from "../../strategy-submission";


class CompetitionProcessStudentRoot extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: 'sample',
            roundsCount: 17,
            prices: {},
            answers: {},
            results: {},
            currentRoundNumber: 5,
            timeTillRoundEnd: 55,
            isCurrentRoundEnded: true,
            teamName: "teamName",
            teamIdInGame: 0,
            messages: [],
            description: "",
            shouldShowResultTable: false,
            shouldShowResultTableInEnd: false,
            unreadMessages: 0,
            isCaptain: false
        }
    }

    componentDidMount() {
        this.fetchCompetitionInfo();
        this.setupAllInOneEvents();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.state.isCurrentRoundEnded) {
            console.log("cleared interval");
            clearInterval(this.timerId);
        } else if (!this.state.isCurrentRoundEnded && prevState.isCurrentRoundEnded) {
            console.log("Cleared interval and launch once again");
            clearInterval(this.timerId);
            this.setupTimer();
        }

        if (this.state.roundsCount > 0
            && this.state.roundsCount === this.state.currentRoundNumber
            && this.state.isCurrentRoundEnded
            && this.state.shouldShowResultTableInEnd
        ) {
            if (this.state.shouldShowResultTableInEnd) {
                this.onRedirectToResultsPage();
            } else if (this.state.isCaptain) {
                    const {pin} = this.props.match.params;
                    this.props.history.push("/competitions/strategy_captain/" + pin);
            }
        }
    }

    onRedirectToResultsPage = () => {
        this.setState(prevState => {
            if (!prevState.didEnd) {
                showNotification(this).success("Игра закончена", "Игра закончена", 2500);

                setTimeout(() => {
                    const {pin} = this.props.match.params;
                    if (prevState.isCaptain) {
                        this.props.history.push("/competitions/strategy_captain/" + pin);
                    } else {
                        this.props.history.push("/competitions/results/" + pin);
                    }
                }, 2500);
                return {didEnd: true};
            }
        });
    };

    setupTimer = () => {
        this.timerId = setInterval(() => {
            console.log("timer event");
            this.setState(prevState => {
                return {timeTillRoundEnd: Math.max(prevState.timeTillRoundEnd - 1, 0)};
            })
        }, 1000);
    };

    componentWillUnmount() {
        if (this.eventSource !== undefined) {
            this.eventSource.close();
        }

        clearInterval(this.timerId);
    }

    submitAnswer = (answer) => {
        const ans = parseInt(answer, 10);
        if (Number.isNaN(ans)) {
            showNotification(this).error("Invalid answer", "Error", 2500);
        } else {
            const obj = {
                answer: ans,
                roundNumber: this.state.currentRoundNumber
            };
            const {pin} = this.props.match.params;

            ApiHelper.submitAnswer(pin, obj).then(resp => {
                console.log({resp});
                if (resp.status >= 300) {
                    return {success: false, json: resp.text()};
                } else {
                    return {success: true, json: resp.json()};
                }
            }).then(resp => {
                resp.json.then(jsonBody => {
                    console.log({jsonBody});
                    if (resp.success) {
                        showNotification(this).success(jsonBody.message, "Success", 2000);
                    } else {
                        showNotification(this).error(jsonBody, "Error", 2000);
                    }
                })
            })
        }
    };

    fetchCompetitionInfo() {
        const {pin} = this.props.match.params;
        ApiHelper.studentCompetitionInfo(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()}
            } else {
                return {success: true, json: resp.json()}
            }
        }).then(resp => {
            resp.json.then(jsonBody => {
                if (resp.success) {
                    this.setState(prevState => {
                        return {
                            teamIdInGame: jsonBody.teamIdInGame,
                            teamName: jsonBody.teamName,
                            roundsCount: jsonBody.roundsCount,
                            description: jsonBody.description,
                            competitionName: jsonBody.name,
                            shouldShowResultTable: jsonBody.shouldShowResultTable,
                            shouldShowResultTableInEnd: jsonBody.shouldShowResultTableInEnd,
                            isCaptain: jsonBody.isCaptain,
                            fetchedStrategy: jsonBody.strategy
                        }
                    })
                }
            })
        })
    }

    processAnswer = (answer) => {
        this.setState((prevState) => {
            let answers = {...prevState.answers};
            answers[answer.roundNumber] = answer.teamAnswer;

            return {answers: answers};
        })
    }

    setupAllInOneEvents() {
        const {pin} = this.props.match.params;

        this.eventSource = ApiHelper.allInOneStudentStream(pin);

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
            }
        })
    }

    processPrice = (data) => {
        this.setState(prevState => {
            let prices = {...prevState.prices};

            prices[data.roundNumber] = data.price;

            return {prices: prices};
        })
    }

    processResult = (data) => {
        this.setState((prevState) => {
            let results = {...prevState.results};
            results[data.roundNumber] = data.income;

            return {results: results};
        })
    }

    processRound = (message) => {
        this.setState(prevState => {
            return processRoundsEvents(message);
        })
    }

    processMessage = (message) => {
        this.setState(prevState => {
            let newState = processMessagesEvents(message, [...prevState.messages]);
            return {...newState, unreadMessages: prevState.unreadMessages + newState.messages.length - prevState.messages.length};
        })
    }

    submitStrategy = (strategy) => {
        const {pin} = this.props.match.params;
        ApiHelper.submitStrategy({strategy}, pin).then(resp => {
            if (resp.status < 300) {
                return {success: true, json: resp.json()}
            } else {
                return {success: false, json: resp.text()}
            }
        }).then(el => {
            el.json.then(jsonBody => {
                if (el.success) {
                    showNotification(this).success(jsonBody.message, "Успех", 2500);
                } else {
                    showNotification(this).error(jsonBody, "Ошибка", 4000);
                }
            })
        })
    }

    render() {
        let instr = this.state.description;
        const {competitionName, roundsCount, prices, answers, results, currentRoundNumber, timeTillRoundEnd} = this.state;

        let table;

        if (this.state.shouldShowResultTable) {
            table = (
                <StudentResultsTable roundsCount={roundsCount} prices={prices} answers={answers} results={results}/>
            );
        } else {
            table = (
                <OneRoundResultsTable roundsCount={roundsCount} prices={prices}
                                      answers={answers} results={results}
                                      currentRoundNumber={this.state.currentRoundNumber}/>
            );
        }

        let sendAnswer;

        if (this.state.isCaptain) {
            sendAnswer = (
                <div style={{paddingTop: "30px"}}>
                    <SendAnswer onSubmit={this.submitAnswer}/>
                </div>
            );
        }
        console.log(this.state)
        const roundText = currentRoundNumber === 0 ? "Игра ещё не началась" : ("Текущий раунд: " + currentRoundNumber + (this.state.isCurrentRoundEnded ? " закончен" : ""));

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "80px"}}>
                    <div style={{textAlign: "center", fontSize: "26px"}}>
                        {"Игра: " + competitionName}
                    </div>
                    <div className={"game-state-holder"}>
                        <div className={"row justify-content-between"}>
                            <div className={"col-4"}>
                                <div>
                                    <div style={{textAlign: "center", fontSize: "23px"}}>
                                        {roundText}
                                    </div>
                                </div>
                            </div>
                            <div className={"col-4"}>
                                <div style={{textAlign: "center", fontSize: "23px"}}>
                                    {"До конца раунда: " + timeTillRoundEnd + "сек"}
                                </div>
                            </div>
                        </div>
                        <div style={{textAlign: "center", fontSize: "26px"}}>
                            {"Команда " + this.state.teamIdInGame + ": " + this.state.teamName}
                        </div>
                        <div style={{paddingTop: "10px", width: "100%"}}>
                            {table}
                        </div>
                        {sendAnswer}
                        <div style={{paddingTop: "30px"}}>
                            <MessagesContainer messages={this.state.messages} unreadMessages={this.state.unreadMessages}
                                onReadMessagesCallback={() => {
                                    this.setState(prevState => {
                                        if (prevState.unreadMessages !== 0)
                                            return {unreadMessages: 0};
                                    })
                                }}
                            />
                        </div>
                        <div style={{paddingTop: "30px"}}>
                            <DescriptionHolder instruction={instr}/>
                        </div>
                        <div style={{paddingTop: "30px"}}>
                            <StrategySubmissionComponent defaultText={this.state.fetchedStrategy}
                                                         isExpanded={false} onChange={(text) => {
                                                             this.setState({fetchedStrategy: text});}}
                                                         onSubmit={this.submitStrategy}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withAuthenticated(CompetitionProcessStudentRoot);
