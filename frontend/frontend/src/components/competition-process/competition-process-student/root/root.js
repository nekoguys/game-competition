import React, {useEffect, useRef, useState} from "react";

import "./root.css";
import {NavbarHeaderWithFetcher as NavbarHeader} from "../../../app/app";
import StudentResultsTable from "../results-table";
import SendAnswer from "../send-answer";
import MessagesContainer from "../messages";
import DescriptionHolder from "../description";
import {processMessageParsedEvent} from "../../../../helpers/messages-event-source-helper";
import {processParsedRoundEvent} from "../../../../helpers/rounds-event-source-helper";
import OneRoundResultsTable from "../one-round-results-table";
import withAuthenticated from "../../../../helpers/with-authenticated";

import * as Constants from "../../../../helpers/constants";
import StrategySubmissionComponent from "../../strategy-submission";
import {useTranslation} from "react-i18next";
import {useNavigate, useParams} from "react-router";

const initialState = {
    competitionName: '',
    roundsCount: 1,
    prices: {},
    answers: {},
    results: {},
    currentRoundNumber: 5,
    timeTillRoundEnd: 55,
    isCurrentRoundEnded: true,
    teamName: "",
    teamIdInGame: 0,
    messages: [],
    description: "",
    shouldShowResultTable: false,
    shouldShowResultTableInEnd: false,
    unreadMessages: 0,
    isCaptain: false
}

const CompetitionProcessStudentRootNew = ({fetchers, eventSources, showNotification}) => {
    const [competitionState, setCompetitionState] = useState(initialState);
    const {t} = useTranslation();
    const {pin} = useParams();
    const isRoundEndedPrevValue = usePrevious(competitionState.isCurrentRoundEnded)
    const timerId = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (competitionState.isCurrentRoundEnded) {
            clearInterval(timerId.current);
        } else if (!competitionState.isCurrentRoundEnded && isRoundEndedPrevValue) {
            clearInterval(timerId.current);
            setupTimer();
        }
    }, [competitionState.isCurrentRoundEnded])

    useEffect(() => {
        if (competitionState.roundsCount > 0
            && competitionState.roundsCount === competitionState.currentRoundNumber
            && competitionState.isCurrentRoundEnded
            && competitionState.shouldShowResultTableInEnd
        ) {
            if (competitionState.shouldShowResultTableInEnd) {
                onRedirectToResultsPage();
            } else if (competitionState.isCaptain) {
                navigate("/competitions/strategy_captain/" + pin);
            }
        }
    })

    useEffect(() => {
        const eventSource = eventSources.allInOneStudentStream(pin);
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
            }
        })

        fetchCompetitionInfo()

        return function cleanup() {
            eventSource.close()
            clearInterval(timerId.current)
        }
    }, [])

    const processAnswer = (answerDTO) => {
        setCompetitionState(prevState => {
            if (answerDTO.type === "cancel") {
                return {...prevState, answers: {}}
            } else {
                let answers = {...prevState.answers};
                answers[answerDTO.roundNumber] = answerDTO.teamAnswer;

                return {...prevState, answers};
            }
        })
    }

    const processMessage = (messageDTO) => {
        setCompetitionState(prevState => {
            const newState = processMessageParsedEvent(messageDTO, [...prevState.messages]);
            return {
                ...prevState, ...newState,
                unreadMessages: prevState.unreadMessages + newState.messages.length - prevState.messages.length
            };
        })
    }

    const processPrice = (priceDTO) => {
        setCompetitionState(prevState => {
            if (priceDTO.type === "cancel") {
                return {...prevState, prices: {}};
            } else {
                const prices = {...prevState.prices};

                prices[priceDTO.roundNumber] = priceDTO.price;

                return {...prevState, prices: prices};
            }
        })
    }

    const processResult = (resultDTO) => {
        setCompetitionState(prevState => {
            if (resultDTO.type === "cancel") {
                return {...prevState, results: {}}
            } else {
                const results = {...prevState.results};
                results[resultDTO.roundNumber] = resultDTO.income
                return {
                    ...prevState,
                    results
                }
            }
        })
    }

    const processRound = (roundDTO) => {
        setCompetitionState(prevState => {
            return {...prevState, ...processParsedRoundEvent(roundDTO)}
        })
    }

    const onRedirectToResultsPage = () => {
        setCompetitionState(prevState => {
            if (!prevState.didEnd) {
                showNotification().success("Game over", "Game over", 2500);

                setTimeout(() => {
                    if (prevState.isCaptain) {
                        navigate("/competitions/strategy_captain/" + pin);
                    } else {
                        navigate("/competitions/results/" + pin);
                    }
                }, 2500);
                return {...prevState, didEnd: true};
            }
        });
    };

    const fetchCompetitionInfo = () => {
        return fetchers.competitionInfo(pin).then(resp => {
            setCompetitionState(prevState => {
                return {
                    ...prevState,
                    teamIdInGame: resp.teamIdInGame,
                    teamName: resp.teamName,
                    roundsCount: resp.roundsCount,
                    description: resp.description,
                    competitionName: resp.name,
                    shouldShowResultTable: resp.shouldShowResultTable,
                    shouldShowResultTableInEnd: resp.shouldShowResultTableInEnd,
                    isCaptain: resp.isCaptain,
                    fetchedStrategy: resp.strategy
                }
            })
        })
    }

    const submitStrategy = () => {
        fetchers.submitStrategy({strategy: competitionState.fetchedStrategy}, pin).then(_ => {
            showNotification().success("Strategy submission completed", "Success", 1500);
        }).catch(err => {
            showNotification().error(err.message, "Error", 2500);
        })
    }

    const submitAnswer = (answerText) => {
        const ans = parseInt(answerText, 10);
        if (Number.isNaN(ans)) {
            showNotification(this).error("Invalid answer", "Error", 2500);
        } else {
            const answerDTO = {
                answer: ans,
                roundNumber: competitionState.currentRoundNumber
            };
            fetchers.submitAnswer(pin, answerDTO).then(resp => {
                showNotification().success(resp.message, "Success", 2000);
            }).catch(err => {
                showNotification().error(err.message ?? "Error occured", "Error", 2000);
            });
        }
    }

    const setupTimer = () => {
        timerId.current = setInterval(() => {
            console.log("timer event");
            setCompetitionState(prevState => {
                return {...prevState, timeTillRoundEnd: Math.max(prevState.timeTillRoundEnd - 1, 0)};
            })
        }, 1000);
    }

    const {
        competitionName,
        roundsCount,
        prices,
        answers,
        results,
        currentRoundNumber,
        timeTillRoundEnd
    } = competitionState;

    let table;

    if (competitionState.shouldShowResultTable) {
        table = (
            <StudentResultsTable roundsCount={roundsCount} prices={prices} answers={answers} results={results}/>
        );
    } else {
        table = (
            <OneRoundResultsTable roundsCount={roundsCount} prices={prices}
                                  answers={answers} results={results}
                                  currentRoundNumber={competitionState.currentRoundNumber}/>
        );
    }

    let sendAnswer;

    if (competitionState.isCaptain) {
        sendAnswer = (
            <div style={{paddingTop: "30px"}}>
                <SendAnswer onSubmit={submitAnswer}/>
            </div>
        );
    }
    const roundText = currentRoundNumber === 0 ?
        t("competition_process.student.root.game_not_started_yet") :
        (t("competition_process.student.root.current_round") + ": " + currentRoundNumber + (competitionState.isCurrentRoundEnded ? " закончен" : ""));

    return (
        <div>
            <div>
                <NavbarHeader/>
            </div>
            <div className={"below-navbar"}>
                <div className={"page-title competition-process-student-page-title"}>
                    {competitionName + " №" + pin}
                </div>
                <div className={"competition-process-student-title-and-game-state-holder-spacer"}/>
                <div className={"competition-process-student-game-state-holder"}>
                    <RoundAndTimerHolder roundNumber={roundText} timeTillRoundEnd={timeTillRoundEnd}/>
                    <div className={"competition-process-student-round-team-name-spacer"}/>
                    <div className={"competition-process-student-team-name"}>
                        {t("competition_process.student.root.team") + competitionState.teamIdInGame + ": " + competitionState.teamName}
                    </div>
                    <div className={"competition-process-student-table-wrapper"}>
                        {table}
                    </div>
                    {sendAnswer}
                    <div style={{paddingTop: "35px"}}>
                        <MessagesContainer messages={competitionState.messages}
                                           unreadMessagesCount={competitionState.unreadMessages}
                                           onReadMessagesCallback={() => {
                                               setCompetitionState(prevState => {
                                                   if (prevState.unreadMessages !== 0)
                                                       return {...prevState, unreadMessages: 0};
                                                   return prevState
                                               })
                                           }}
                        />
                    </div>
                    <div style={{paddingTop: "20px"}}>
                        <DescriptionHolder instruction={competitionState.description}/>
                    </div>
                    <div style={{paddingTop: "20px"}}>
                        <StrategySubmissionComponent
                            strategyText={competitionState.fetchedStrategy}
                            isExpanded={false} onChange={(text) => {
                            setCompetitionState(prevValue => {
                                return {...prevValue, fetchedStrategy: text}
                            });
                        }}
                            onSubmit={() => submitStrategy()}/>
                    </div>
                </div>
            </div>
        </div>
    )
}

function usePrevious(value) {
    const ref = useRef();
    useEffect(() => {
        ref.current = value;
    }, [value]);
    return ref.current;
}

const RoundAndTimerHolder = ({roundNumber, timeTillRoundEnd}) => {
    const {t} = useTranslation();

    return <>
        <div className={"competition-process-student-round-and-time-container"}>
            <div>
                {roundNumber}
            </div>
            <div>
                {t("competition_process.student.root.until_end") + timeTillRoundEnd + t("competition_process.student.root.seconds")}
            </div>
        </div>
    </>
}

export default withAuthenticated(CompetitionProcessStudentRootNew);

