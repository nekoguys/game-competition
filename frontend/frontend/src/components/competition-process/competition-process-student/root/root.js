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
            isCurrentRoundEnded: false,
            teamName: "teamName",
            teamIdInGame: 0,
            messages: [],
            description: "",
            shouldShowResultTable: false,
            unreadMessages: 0,
            isCaptain: false
        }
    }

    componentDidMount() {
        this.fetchCompetitionInfo();
        this.setupMessagesStream();
        this.setupRoundEventsStream();
        this.setupResultsEventsStream();
        this.setupPricesEventsStream();
        this.setupMyAnswersEventsStream();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.state.isCurrentRoundEnded) {
            clearInterval(this.timerId);
        } else if (!this.state.isCurrentRoundEnded && prevState.isCurrentRoundEnded) {
            clearInterval(this.timerId);
            this.setupTimer();
        }

        if (this.state.roundsCount > 0 && this.state.roundsCount === this.state.currentRoundNumber && this.state.isCurrentRoundEnded) {
            this.onRedirectToResultsPage();
        }
    }

    onRedirectToResultsPage = () => {
        this.setState(prevState => {
            if (!prevState.didEnd) {
                showNotification(this).success("Игра закончена", "Игра закончена", 2500);

                setTimeout(() => {
                    const {pin} = this.props.match.params;
                    this.props.history.push("/competitions/results/" + pin);
                }, 2500);
                return {didEnd: true};
            }
        });
    };

    setupTimer = () => {
        this.timerId = setInterval(() => {
            this.setState(prevState => {
                return {timeTillRoundEnd: Math.max(prevState.timeTillRoundEnd - 1, 0)};
            })
        }, 1000);
    };

    componentWillUnmount() {
        if (this.messagesEventSource !== undefined) {
            this.messagesEventSource.close();
        }
        if (this.roundEventsSource !== undefined) {
            this.roundEventsSource.close();
        }
        if (this.resultsEventsSource !== undefined) {
            this.resultsEventsSource.close();
        }
        if (this.pricesEventSource !== undefined) {
            this.pricesEventSource.close();
        }
        if (this.answersEventSource !== undefined) {
            this.answersEventSource.close();
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
                            isCaptain: jsonBody.isCaptain
                        }
                    })
                }
            })
        })
    }

    setupMyAnswersEventsStream() {
        const {pin} = this.props.match.params;

        this.answersEventSource = ApiHelper.myAnswersStream(pin);

        this.answersEventSource.addEventListener("error", (err) => console.log({answersEventSourceErr: err}));

        this.answersEventSource.addEventListener("message", (message) => {
            const data = JSON.parse(message.data);

            this.setState((prevState) => {
                let answers = {...prevState.answers};
                answers[data.roundNumber] = data.teamAnswer;

                return {answers: answers};
            })
        })
    }

    setupPricesEventsStream() {
        const {pin} = this.props.match.params;

        this.pricesEventSource = ApiHelper.competitionRoundPricesStream(pin);

        this.pricesEventSource.addEventListener("erorr", err => console.log({pricesEventSourceErr: err}));

        this.pricesEventSource.addEventListener("message", (message) => {
            const data = JSON.parse(message.data);

            this.setState(prevState => {
                let prices = {...prevState.prices};

                prices[data.roundNumber] = data.price;

                return {prices: prices};
            })
        })
    }

    setupResultsEventsStream() {
        const {pin} = this.props.match.params;

        this.resultsEventsSource = ApiHelper.myResultsStream(pin);

        this.resultsEventsSource.addEventListener("error", err => console.log({resultsEventSourceErr: err}));

        this.resultsEventsSource.addEventListener("message", (message) => {
            const data = JSON.parse(message.data);
            this.setState((prevState) => {
                let results = {...prevState.results};
                results[data.roundNumber] = data.income;

                return {results: results};
            })
        });
    }

    setupRoundEventsStream() {
        const {pin} = this.props.match.params;

        this.roundEventsSource = ApiHelper.competitionRoundEventsStream(pin);

        this.roundEventsSource.addEventListener("error", (err) => {
            console.log("roundsEventSource error");
            console.log({err});
        });

        this.roundEventsSource.addEventListener("message", (message) => {
            this.setState(prevState => {
                return processRoundsEvents(message);
            })
        });
    }

    setupMessagesStream() {
        const {pin} = this.props.match.params;

        this.messagesEventSource = ApiHelper.competitionMessagesEventSource(pin);

        this.messagesEventSource.addEventListener("error", (err) => {
            console.log("messagesEventSource error");
            console.log({err});
        });

        this.messagesEventSource.addEventListener("message", (message) => {
            this.setState(prevState => {
                let newState = processMessagesEvents(message, [...prevState.messages]);
                return {...newState, unreadMessages: prevState.unreadMessages + newState.messages.length - prevState.messages.length};
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
                                        {"Текущий раунд: " + currentRoundNumber + (this.state.isCurrentRoundEnded ? " закончен" : "")}
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
                    </div>
                </div>
            </div>
        )
    }
}

export default withAuthenticated(CompetitionProcessStudentRoot);
