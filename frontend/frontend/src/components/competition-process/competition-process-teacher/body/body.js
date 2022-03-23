import React, {useEffect, useRef, useState} from "react";

import "./body.css"
import DefaultSubmitButton from "../../../common/default-submit-button";
import CompetitionResultsTable from "../results-table";
import MessagesContainer from "../messages";
import {processParsedRoundEvent} from "../../../../helpers/rounds-event-source-helper";
import {processMessageParsedEvent} from "../../../../helpers/messages-event-source-helper";
import * as Constants from "../../../../helpers/constants";
import ChangeRoundLengthContainer from "../change-round-length-container";
import {useTranslation} from "react-i18next";
import usePrevious from "../../../../helpers/use-previous-hook";
import {RoundAndTimerHolder} from "../../competition-process-student/root/root";
import {CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {SelfControlledDefaultToggle} from "../../competition-process-student/messages/messages";

const defaultState = {
    currentRoundNumber: 0,
    timeTillRoundEnd: 0,
    isCurrentRoundEnded: true,
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
}

const CompetitionProcessTeacherBodyNew = (
    {
        showNotification,
        onEndCallback,
        updateCompetitionNameCallback,
        pin,
        fetchers,
        eventSources
    }) => {
    const [competitionState, setCompetitionState] = useState(defaultState);
    const {i18n} = useTranslation();
    const timerId = useRef(null);
    const isRoundEndedPrevValue = usePrevious(competitionState.isCurrentRoundEnded)

    const getCompetitionInfo = () => {
        fetchers.competitionInfoForResultsTable(pin).then(resp => {
            updateCompetitionNameCallback(resp.name);


            setCompetitionState((prevState => {
                if (resp.roundsCount === prevState.currentRoundNumber && resp.roundsCount !== 0) {
                    onEndCallback();
                }
                return {
                    ...prevState,
                    name: resp.name,
                    teamsCount: resp.connectedTeamsCount,
                    roundsCount: resp.roundsCount,
                    isAutoRoundEnding: resp.isAutoRoundEnding
                }
            }))

        })
    }

    const updateCertainFields = (obj) => {
        setCompetitionState(prevValue => {
            return {...prevValue, ...obj}
        })
    }

    const setupTimer = () => {
        timerId.current = setInterval(() => {
            setCompetitionState(prevState => {
                return {...prevState, timeTillRoundEnd: Math.max(prevState.timeTillRoundEnd - 1, 0)};
            })
        }, 1000);
    }

    useEffect(() => {
        getCompetitionInfo();
        const eventSource = eventSources.allInOneTeacherStream(pin);
        eventSource.subscribe((message) => {
            console.log({messageDataExt: message});
            console.log({messageDataExt: message.id});
            if (message.lastEventId === Constants.ANSWER_EVENT_ID) {
                processAnswer(message);
            } else if (message.lastEventId === Constants.MESSAGE_EVENT_ID) {
                processMessage(message);
            } else if (message.lastEventId === Constants.PRICE_EVENT_ID) {
                processPrice(message);
            } else if (message.lastEventId === Constants.RESULT_EVENT_ID) {
                processResult(message);
            } else if (message.lastEventId === Constants.ROUND_EVENT_ID) {
                processRound(message)
            } else if (message.lastEventId === Constants.BAN_EVENT_ID) {
                processBan(message);
            }
        })

        return function cleanup() {
            eventSource.close();
        }
    }, []);

    useEffect(() => {
        if (competitionState.isCurrentRoundEnded) {
            clearInterval(timerId.current);
        } else if (!competitionState.isCurrentRoundEnded && isRoundEndedPrevValue) {
            clearInterval(timerId.current);
            setupTimer();
        }
    }, [competitionState.isCurrentRoundEnded])

    useEffect(() => {
        if (competitionState.roundsCount > 0 && competitionState.roundsCount === competitionState.currentRoundNumber && competitionState.isCurrentRoundEnded) {
            onEndCallback();
        }
    });

    const processBan = (banDTO) => {
        if (banDTO.type === "cancel") {
            updateCertainFields({bannedTeams: []})
        } else {
            setCompetitionState(prevState => {
                return {...prevState, bannedTeams: [...prevState.bannedTeams, banDTO.teamIdInGame]}
            });
            showNotification().warning(`Team ${banDTO.teamIdInGame} "${banDTO.teamName}" was banned`, 'BAN', 3000);
        }
    }

    const processPrice = (priceDTO) => {
        if (priceDTO.type === "cancel") {
            updateCertainFields({prices: {}});
        } else {
            setCompetitionState(prevState => {
                return {...prevState, prices: {...prevState.prices, [priceDTO.roundNumber]: priceDTO.price}}
            })
        }
    }

    const processResult = (resultDTO) => {
        if (resultDTO.type === "cancel") {
            updateCertainFields({results: {}});
        } else {
            setCompetitionState(prevValue => {
                const results = {...prevValue.results};
                if (resultDTO.roundNumber in results) {
                    results[resultDTO.roundNumber][resultDTO.teamIdInGame] = resultDTO.income;
                } else {
                    results[resultDTO.roundNumber] = {[resultDTO.teamIdInGame]: resultDTO.income}
                }
                return {...prevValue, results};
            })
        }
    }

    const processAnswer = (answerDTO) => {
        if (answerDTO.type === "cancel") {
            updateCertainFields({answers: {}});
        } else {
            const teamIdInGame = answerDTO.teamIdInGame;
            const round = answerDTO.roundNumber;
            const answer = answerDTO.teamAnswer;

            setCompetitionState(prevState => {
                const answers = {...prevState.answers};

                if (!(round in answers)) {
                    answers[round] = {[teamIdInGame]: answer}
                } else {
                    answers[round][teamIdInGame] = answer;
                }

                return {...prevState, answers};
            })
        }
    }

    const processRound = (message) => {
        updateCertainFields(processParsedRoundEvent(message))
    }

    const processMessage = (message) => {
        setCompetitionState(prevState => {
            const newMessages = processMessageParsedEvent(message, [...prevState.messages]);
            return {...prevState, ...newMessages};
        })
    }

    const sendMessage = (message) => {
        if (message !== "") {
            fetchers.sendMessage(pin, message).then(_ => {
                showNotification().success("Message sent successfully", "Success", 1500);
            }).catch(err => {
                showNotification().error(err.message, "Error", 2500);
            })
        }
    }

    const startOrEndRound = () => {
        const fetchPromise = competitionState.isCurrentRoundEnded
            ? fetchers.startNewCompetitionRound(pin)
            : fetchers.endCompetitionRound(pin)

        fetchPromise.then(resp => {
            showNotification().success(resp.message, "Success", 1500);
        }).catch(err => {
            showNotification().error(err.message, "Error", 2500);
        })
    }

    const changeRoundLength = (newRoundLength) => {
        const ans = parseInt(newRoundLength, 10);
        if (Number.isNaN(ans)) {
            showNotification(this).error("Invalid round length", "Error", 2500);
        } else {
            fetchers.changeRoundLength(pin, ans).then(resp => {
                showNotification().success(resp.message, "Success", 1500);
            }).catch(err => {
                showNotification().error(err.message, "Error", 2500);
            })
        }
    }

    const restartGame = () => {
        fetchers.restartGame(pin).then(resp => {
            showNotification().success(resp.message, "Success", 1500);
        }).catch(err => {
            showNotification().error(err.message, "Error", 2500);
        })
    }

    let res = (<CompetitionProcessTeacherActive
        i18n={i18n}
        round={competitionState.currentRoundNumber}
        timeLeft={competitionState.timeTillRoundEnd}
        isRoundEnded={competitionState.isCurrentRoundEnded}
        messages={competitionState.messages}
        answers={competitionState.answers}
        sendMessageCallBack={(message) => sendMessage(message)}
        rightButtonClick={() => startOrEndRound()}
        teamsCount={competitionState.teamsCount}
        roundsCount={competitionState.roundsCount}
        results={competitionState.results}
        prices={competitionState.prices}
        bannedTeams={competitionState.bannedTeams}
        currentRoundLength={competitionState.currentRoundLength}
        changeRoundLengthCallback={(roundLength) => changeRoundLength(roundLength)}
        isAutoRoundEnding={competitionState.isAutoRoundEnding}
        restartGameCallback={restartGame}
    />);

    return (
        <div>
            <div className={"game-state-holder"}>
                {res}
            </div>
        </div>
    )
}

class CompetitionProcessTeacherActive extends React.Component {
    render() {
        
        const {round, timeLeft, isRoundEnded} = this.props;


        const rightButtonText = round === 0 ? 
        this.props.i18n.t("competition_process.teacher.body.start_game") : (isRoundEnded ? this.props.i18n.t("competition_process.teacher.body.start_new_round") : this.props.i18n.t("competition_process.teacher.body.end_round"));

        const roundText = round === 0 ? 
            this.props.i18n.t("competition_process.teacher.body.game_not_started_yet") : 
            (this.props.i18n.t("competition_process.teacher.body.current_round") + round.toString() + (isRoundEnded ? this.props.i18n.t("competition_process.teacher.body.ended") : ""));

        console.log({props: this.props});

        let beginEndRoundButton;
        if (!this.props.isAutoRoundEnding || this.props.round === 0) {
            beginEndRoundButton = (
                <div>
                    <div className={"competition-process-teacher-actions-buttons-top-spacer"}/>
                    <DefaultSubmitButton text={rightButtonText} onClick={this.props.rightButtonClick}/>
                </div>
            )
        }

        let restartGameButton;

        if (this.props.round !== 0 && !(this.props.isRoundEnded && this.props.round === this.props.roundsCount)) {
            restartGameButton = (
                // <div style={{paddingTop: "20px"}}>
                //     <DefaultSubmitButton text={this.props.i18n.t("competition_process.teacher.body.restart_game")}
                //                          onClick={this.props.restartGameCallback}/>
                // </div>
                <div/>
            )
        }
        const pricesGraphData = Object.entries(this.props.prices)
            .sort((lhs, rhs) => lhs[0] - rhs[0])
            .map(([roundNumber, price]) => {
                return {roundNumber, price}
            })

        const productionGraphData = Object.entries(this.props.answers)
            .map(([key, value]) => {
                return {
                    roundNumber: key,
                    production: Object.values(value).reduce((a, b) => a + b, 0).toFixed(1)
                }
            })
        console.log({productionGraphData})
        return (
            <div>
                <RoundAndTimerHolder roundNumber={roundText} timeTillRoundEnd={timeLeft}/>
                <div className={"competition-process-teacher-buttons-holder"}>
                    {restartGameButton}
                    {beginEndRoundButton}
                </div>
                <div className={"competition-process-teacher-buttons-results-table-spacer"}/>
                <div className={"competition-process-teacher-results-table-holder"}>
                    <CompetitionResultsTable style={{width: "100%"}} teamsCount={this.props.teamsCount}
                                             roundsCount={this.props.roundsCount}
                                             answers={this.props.answers}
                                             results={this.props.results}
                                             prices={this.props.prices}
                                             bannedTeams={this.props.bannedTeams}
                    />
                </div>
                <div className={"competition-process-teacher-bottom-content"}>
                    <div className={"competition-process-teacher-results-table-round-length-spacer"}/>
                    <ChangeRoundLengthContainer currentRoundLength={this.props.currentRoundLength}
                                                changeRoundLengthCallback={this.props.changeRoundLengthCallback}/>
                    <div className={"competition-process-teacher-round-length-messages-spacer"}/>
                    <MessagesContainer messages={this.props.messages}
                                       sendMessageCallBack={this.props.sendMessageCallBack}/>
                    <div className={"competition-process-teacher-price-graph-container-spacer"}/>
                    <SelfControlledDefaultToggle title={"Графики"}>
                        <div className={"competition-process-teacher-price-graph-container-top-spacer"}/>
                        <div className={"competition-process-teacher-price-graph-container"}>
                            <div className={"competition-process-teacher-price-graph-title"}>График цены</div>
                            <div className={"competition-process-teacher-price-graph-wrapper"}>
                                <GenericGraph data={pricesGraphData} dataKey={"price"} valuePrefix={"Цена"}/>
                            </div>
                        </div>
                        <div className={"competition-process-teacher-price-graph-container-spacer"}/>
                        <div className={"competition-process-teacher-price-graph-container"}>
                            <div className={"competition-process-teacher-price-graph-title"}>График производства</div>
                            <div className={"competition-process-teacher-price-graph-wrapper"}>
                                <GenericGraph data={productionGraphData} dataKey={"production"}
                                              valuePrefix={"Производство"}/>
                            </div>
                        </div>
                    </SelfControlledDefaultToggle>

                </div>
            </div>
        )
    }
}

const GenericGraph = ({data, dataKey, valuePrefix}) => {
    // const data = Object.entries(prices)
    //     .sort((lhs, rhs) => lhs[0] - rhs[0])
    //     .map(([roundNumber, price]) =>{ return { roundNumber, price } })
    // console.log({data, entries: Object.entries({1: 15, 2: 20, 3: 25, 4: 18})});

    return (
        <ResponsiveContainer width={"100%"} height={"100%"}>
            <LineChart data={data} height={300} width={600}>
                <CartesianGrid stroke="#ccc" fill={"#fff"} fillOpacity={0.8}/>
                <Line type="monotone" dataKey={dataKey} stroke="#BBC2D3" strokeWidth={3}/>
                <XAxis {...defaultXAxisParams}/>
                <Tooltip {...defaultTooltipParams(valuePrefix)} />
                <YAxis tick={{fill: "rgba(0, 0, 0, 0.6)"}} type={"number"}
                       domain={[dataMin => (dataMin / 3 * 2).toFixed(1), dataMax => (dataMax + dataMax / 5).toFixed(1)]}/>
            </LineChart>
        </ResponsiveContainer>

    )
}

const defaultXAxisParams = {
    tick: {
        fill: "rgba(0, 0, 0, 0.6)"
    },
    dataKey: "roundNumber"
}

const defaultTooltipParams = (valuePrefix) => {
    return {
        labelFormatter: (label) => `Раунд: ${label}`,
        formatter: (value, _, __) => [`${valuePrefix}: ${value}`, null]
    }
}

export default (CompetitionProcessTeacherBodyNew);
