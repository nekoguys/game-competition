import React from "react";
import {BrowserRouter as Router, Navigate, Route, Routes} from "react-router-dom";
import Login from "../auth/login";
import Register from "../auth/register/register";
import CompetitionHistory from "../competition-history/competition-history";
import JoinCompetition from "../join-competition/join-competition";
import AfterRegistrationOpenedComponent from "../after-registration-opened";
import WaitingRoom from "../join-competition/waiting-room";
import CompetitionProcessTeacherRootComponent from "../competition-process/competition-process-teacher/root";
import ForbiddenError from "../errors/forbidden-error";
import CompetitionProcessStudentRoot from "../competition-process/competition-process-student/root";
import Verification from "../auth/verification/verification";
import EndedCompetitionResultsRoot from "../competition-process/ended-competition-results/root";
import UserProfileRoot from "../user-profile/root";

import {NotificationContainer, NotificationManager} from "react-notifications";
import AdminkaComponent from "../adminka";
import FinalStrategySubmissionComponent
    from "../competition-process/ended-competition-results/final-strategy-submission";
import apiFetcher from "../../helpers/api-fetcher";
import ApiHelper from "../../helpers/api-helper";
import isAuthenticated, {getUTCSeconds} from "../../helpers/is-authenticated";
import OAuthLogin from "../auth/oauth-login";
import {LocalizationHelper} from "../../helpers/localization-helper";
import NavbarHeader from "../competition-history/navbar-header";
import NewJoinCompetitionCaptainForm
    from "../join-competition/join-competition-form/new-forms/new-join-competition-captain-form";
import NewJoinCompetitionMemberForm
    from "../join-competition/join-competition-form/new-forms/new-join-competition-member-form/new-join-competition-member-form";
import CompetitionProcessTeacherBody from "../competition-process/competition-process-teacher/body";
import {LocalStorageWrapper} from "../../helpers/storage-wrapper";
import CreateCompetition from "../create-competition/create-competition";
import EventSourceMock from "../../helpers/mocks/event-source-mock";
import EventSourceWrapper from "../../helpers/event-source-wrapper";
import {isTeacher} from "../../helpers/role-helper";
import * as Constants from "../../helpers/constants";

const fetcherType = ApiHelper.fetcherType();

const signinFetcher = {
    mock: (_) => {
        return new Promise(resolve =>
            resolve({accessToken: "1", email: "someEmail", authorities: ['Student'], expirationTimestamp: getUTCSeconds(new Date(Date.now() + 24*60*60*1000))})
        )
    },
    real: (params) => { return apiFetcher(params, (credentials) => ApiHelper.signin(credentials)) }
}[fetcherType];

const signupFetcher = {
    mock: (_) => {
        return new Promise(resolve =>
            resolve({})
        )
    },
    real: (params) => { return apiFetcher(params, (credentials) => { return ApiHelper.signup(credentials) }) }
}[fetcherType];

const verificationFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({message: "Подтвержден"})
        }, 2500))
    },
    real: (token) => { return apiFetcher(token, (token) => ApiHelper.accountVerification(token)) }
}[fetcherType];

const competitionProcessStudentCompetitionInfoFetcher = {
    mock: (_) => {
        return new Promise(resolve => resolve({
            teamIdInGame: 1,
            teamName: "Sample team nae",
            roundsCount: 20,
            description: "sample description",
            name: "Конкуренция на рынке пшеницы",
            shouldShowResultTable: true,
            shouldShowResultTableInEnd: true,
            isCaptain: true,
            fetchedStrategy: null
        }))
    },
    real: (pin) => {
        return apiFetcher(pin, (pin) => ApiHelper.studentCompetitionInfo(pin))
    }
}[fetcherType]

const allInOneStudentStream = {
    mock: (_) => {
        return new EventSourceMock([
            {
                lastEventId: Constants.ROUND_EVENT_ID,
                type: "newround",
                roundLength: 66,
                roundNumber: 1,
                beginTime: (Math.round((new Date().getTime()) / 1000))
            },
            {
                lastEventId: Constants.ANSWER_EVENT_ID,
                roundNumber: 1,
                teamAnswer: 10
            },
            {
                lastEventId: Constants.RESULT_EVENT_ID,
                roundNumber: 1,
                income: 100.1
            },
            {
                lastEventId: Constants.PRICE_EVENT_ID,
                roundNumber: 1,
                price: 15
            },
            {
                lastEventId: Constants.MESSAGE_EVENT_ID,
                message: "sample message sample message sample message",
                sendTime: 1647732566
            }
        ], 200)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.allInOneStudentStream(pin))
    }
}[fetcherType]

const userInfoFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({userDescription: "Иванов И.И."})
        }))
    },
    real: () => { return apiFetcher({}, (_) => ApiHelper.getNavBarInfo()) }
}[fetcherType]

export const NavbarHeaderWithFetcher = (props) => {
    return <NavbarHeader userInfoFetcher={userInfoFetcher} {...props}/>
}

const historyFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve([
                {
                    name: "Конкуренция на рынке пшеницы",
                    state: "Registration",
                    pin: "1234",
                    isOwned: false
                },
                {
                    name: "Ко",
                    state: "InProcess",
                    pin: "12345",
                    isOwned: false
                }
            ])
        }))
    },
    real: (start, count) => { return apiFetcher([start, count],
        ([start, count]) => ApiHelper.competitionsHistory(start / count, count)) }
}[fetcherType]

const pinFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({exists: true})
        }))
    },
    real: (pin) => { return apiFetcher(pin, (pin) => ApiHelper.checkPin(pin)) }
}[fetcherType]

const createTeamFetcher = {
    mock: (pin, _) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({})
        }))
    },
    real: (pin, data) => { return apiFetcher(data, (teamData) => ApiHelper.createTeam(pin, teamData)) }
}[fetcherType]

const joinTeamFetcher = {
    mock: (_, {teamName}) => {
        return new Promise(resolve => resolve({currentTeamName: teamName}))
    },
    real: (pin, data) => { return apiFetcher(data, (data) => ApiHelper.joinTeam(pin, data)) }
}[fetcherType]

const createCompetitionFetcher = {
    mock: (_) => {
        return new Promise(resolve => resolve({pin: 1234}))
    },
    real: (obj) => {
        return apiFetcher(obj, (data) => ApiHelper.createCompetition(data))
    }
}[fetcherType];

const updateCompetitionFetcher = {
    mock: (_, obj) => {
        return new Promise(resolve => resolve({
            ...obj,
            name: "edited:" + obj.name,
            demandFormula: obj.demandFormula.join(';'),
            expensesFormula: obj.expensesFormula.join(';')
        }
        )
        )
    },
    real: (pin, obj) => {
        return apiFetcher({pin, obj}, (data) => ApiHelper.updateCompetition(data.pin, data.obj))
    }
}[fetcherType];

const startCompetitionFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => resolve({}), 600))
    },
    real: (pin) => {
        return apiFetcher(pin, (pin) => ApiHelper.startCompetition(pin))
    }
}[fetcherType]

const teamNameAndPasswordFetcher = {
    mock: (_) => {
        return new Promise(resolve => {
            setTimeout(() => resolve({password: "someSeCrEtPaSsWoRd", teamName: "Капитанская дочка"}), 500)
        })
    },
    mockFailed: (_) => {
        return new Promise(resolve => {
            setTimeout(() => resolve({password: null, teamName: "Капитанская дочка"}), 500)
        })
    },
    real: (pin) => {
        return apiFetcher(pin, (pin) => ApiHelper.getCurrentTeam(pin))
    }
}[fetcherType]

const teamMembers = {
    mock: (_) => {
        return new EventSourceMock([
            {
                name: "Гринев ПА - grinev@edu.hse.ru",
                isCaptain: false
            },
            {
                name: "Швабрин АИ - shwabrin@edu.hse.ru",
                isCaptain: false
            }
        ], 200)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.myTeamsNewMembersEventSource(pin));
    }
}[fetcherType]


const competitionRoundEvents = {
    mock: (_) => {
        return new EventSourceMock([])
    },
    mockEnding: (_) => {
        return new EventSourceMock([{}], 3000)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.competitionRoundEventsStream(pin));
    }
}[fetcherType]

const submitAnswer = {
    mock: (_) => new Promise(resolve => resolve({})),
    real: (pin, answerDTO) => apiFetcher({pin, answerDTO}, ({pin, answerDTO}) => ApiHelper.submitAnswer(pin, answerDTO))
}[fetcherType]

const submitStrategy = {
    mock: (_) => new Promise(resolve => resolve({})),
    real: (strategyDTO, pin) => apiFetcher(strategyDTO, (strategyDTO) => ApiHelper.submitStrategy(strategyDTO, pin))
}[fetcherType]

const cloneInfoCompetitionFetcher = {
    mock: (_) => {
        return new Promise(resolve => {
            setTimeout(() => resolve(
                {
                    name: "sampleName",
                    expensesFormula: "1;2;3",
                    demandFormula: "1;2",
                    maxTeamsAmount: 11,
                    maxTeamSize: 2,
                    roundsCount: 5,
                    instruction: "some instruction",
                    teamLossUpperbound: 1000,
                    shouldShowStudentPreviousRoundResults: true,
                    shouldShowResultTableInEnd: true,
                    isAutoRoundEnding: true
                }
            ), 1000)
        })
    },
    real: (pin) => {
        return apiFetcher(pin, (pin) => ApiHelper.getCompetitionCloneInfo(pin))
    }
}[fetcherType]


const allInOneTeacherStream = {
    mock: (_) => {
        return new EventSourceMock([
            {
                lastEventId: Constants.ROUND_EVENT_ID,
                type: "newround",
                roundLength: 66,
                roundNumber: 1,
                beginTime: (Math.round((new Date().getTime()) / 1000))
            },
            {
                lastEventId: Constants.ANSWER_EVENT_ID,
                roundNumber: 1,
                teamAnswer: 10,
                teamIdInGame: 1,
            },
            {
                lastEventId: Constants.RESULT_EVENT_ID,
                roundNumber: 1,
                income: 100.1,
                teamIdInGame: 1
            },
            {
                lastEventId: Constants.PRICE_EVENT_ID,
                roundNumber: 1,
                price: 15
            },
            {
                lastEventId: Constants.MESSAGE_EVENT_ID,
                message: "sample message sample message sample message",
                sendTime: 1647732566
            }
        ], 200)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.allInOneTeacherStream(pin))
    }
}[fetcherType]

const competitionInfoForResultsTableFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() =>
            resolve({
                name: "Конкуренция на рынке пшеницы",
                teamsCount: 5,
                roundsCount: 7,
                isAutoRoundEnding: false,
                connectedTeamsCount: 10
            }), 1500))
    },
    real: (pin) => apiFetcher(pin, (pin) => ApiHelper.competitionInfoForResultsTable(pin))
}[fetcherType]

const startNewCompetitionRound = {
    mock: (_) => {
        new Promise(resolve => resolve({}));
    },
    real: (pin) => apiFetcher(pin, (pin) => ApiHelper.startNewCompetitionRound(pin))
}[fetcherType]

const endCompetitionRound = {
    mock: (_) => {
        new Promise(resolve => resolve({}));
    },
    real: (pin) => apiFetcher(pin, (pin) => ApiHelper.endCompetitionRound(pin))
}[fetcherType]

const sendMessage = {
    mock: (_) => {
        return new Promise(resolve => resolve({}));
    },
    real: (pin, message) => apiFetcher({pin, message}, ({
                                                            pin,
                                                            message
                                                        }) => ApiHelper.sendCompetitionMessage(pin, message))
}[fetcherType]

const changeRoundLength = {
    mock: (_) => {
        return new Promise(resolve => resolve({}))
    },
    real: (pin, newLength) => apiFetcher({pin, newLength}, ({
                                                                pin,
                                                                newLength
                                                            }) => ApiHelper.changeRoundLength(pin, newLength))
}[fetcherType]

export const CompetitionProcessTeacherBodyNew = (props) => {
    return <CompetitionProcessTeacherBody
        fetchers={{
            competitionInfoForResultsTable: competitionInfoForResultsTableFetcher,
            startNewCompetitionRound,
            endCompetitionRound,
            sendMessage,
            changeRoundLength
        }} eventSources={{allInOneTeacherStream}}
        {...props}
    />
}

const teamEventsSource = {
    mock: (_) => {
        return new EventSourceMock([
            {
                teamName: "Команда Команда Команда",
                teamMembers: ["Вася", "Кука"],
                idInGame: 1
            },
            {
                teamName: "Команда2",
                teamMembers: ['Бука', 'Злюка'],
                idInGame: 2
            },
            {
                teamName: "Команда3",
                teamMembers: ['Гена Букин', 'Клава Кока'],
                idInGame: 3
            },
            {
                teamName: "Команда2",
                teamMembers: ['Бука', 'Злюка', 'Бяка'],
                idInGame: 2
            },
        ], 200)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.teamCreationEventSource(pin));
    }
}[fetcherType]

const paths = [
    {
        path: "/auth/signin",
        component: Login,
        props: {
            fetchers: {
                submit: signinFetcher
            },
            authProvider: {
                isAuthenticated: () => isAuthenticated()
            },
            onSuccessfulLogin: (resp) => {
                const storage = window.localStorage;
                storage.setItem("accessToken", resp.accessToken);
                storage.setItem("user_email", resp.email);
                storage.setItem("roles", resp.authorities.map(el => el.authority));
                storage.setItem("expirationTimestamp", resp.expirationTimestamp);
            }
        }
    },
    {
        path: "/auth/oauth",
        component: OAuthLogin,
        props: {
            localizationHelper: new LocalizationHelper()
        }
    },
    {
        path: "/auth/signup",
        component: Register,
        props: {
            fetchers: {
                submit: signupFetcher
            }
        }
    },
    {
        path: "/competitions/history",
        component: CompetitionHistory,
        props: {
            fetchers: {
                history: historyFetcher,
                pinCheckFetcher: pinFetcher
            },
            isTeacher: isTeacher
        }
    },
    {
        path: "/competitions/create",
        component: CreateCompetition,
        props: {
            fetchers: {
                createCompetition: createCompetitionFetcher,
                updateCompetition: updateCompetitionFetcher,
            },
            isUpdateMode: false
        }
    },
    {
        path: "/competitions/draft_competition/:pin",
        component: CreateCompetition,
        props: {
            fetchers: {
                createCompetition: createCompetitionFetcher,
                updateCompetition: updateCompetitionFetcher,
            },
            isUpdateMode: true
        }
    },
    {
        path: "/competitions/join",
        component: JoinCompetition
    },
    {
        path: "/competitions/join-new-captain/:pin",
        component: NewJoinCompetitionCaptainForm,
        props: {
            fetchers: {
                createTeam: createTeamFetcher
            },
            captainEmailProvider: new LocalStorageWrapper("user_email", null)
        }
    },
    {
        path: "/competitions/join-new-member/:pin",
        component: NewJoinCompetitionMemberForm,
        props: {
            fetchers: {
                joinTeam: joinTeamFetcher
            },
            eventSources: {
                teams: teamEventsSource
            }
        }
    },
    {
        path: "/competitions/after_registration_opened/:pin",
        component: AfterRegistrationOpenedComponent,
        props: {
            fetchers: {
                updateCompetition: updateCompetitionFetcher,
                startCompetition: startCompetitionFetcher,
                cloneInfo: cloneInfoCompetitionFetcher,
            },
            eventSources: {
                teams: teamEventsSource,

            }
        }
    },
    {
        path: "/competitions/waiting_room/:pin",
        component: WaitingRoom,
        props: {
            fetchers: {
                teamNameAndPassword: teamNameAndPasswordFetcher
            },
            eventSources: {
                teamMembers: teamMembers,
                competitionRoundEvents: competitionRoundEvents
            }
        }
    },
    {
        path: "/competitions/process_teacher/:pin",
        component: CompetitionProcessTeacherRootComponent
    },
    {
        path: "/forbidden",
        component: ForbiddenError
    },
    {
        path: "/competitions/process_captain/:pin",
        component: CompetitionProcessStudentRoot,
        props: {
            fetchers: {
                competitionInfo: competitionProcessStudentCompetitionInfoFetcher,
                submitAnswer: submitAnswer,
                submitStrategy: submitStrategy
            },
            eventSources: {
                allInOneStudentStream: allInOneStudentStream
            }
        }
    },
    {
        path: "/auth/verification/:token",
        component: Verification,
        props: {
            fetchers: {
                verify: verificationFetcher
            }
        }
    },
    {
        path: "/competitions/results/:pin",
        component: EndedCompetitionResultsRoot
    },
    {
        path: "/profile",
        component: UserProfileRoot
    },
    {
        path: "/adminka",
        component: AdminkaComponent
    },
    {
        path: "/competitions/strategy_captain/:pin",
        component: FinalStrategySubmissionComponent
    }
];

export default function App() {
    return (
        <div>
            <Router>
                <Routes>
                    <Route path={"/"} element={<Navigate to="/auth/signin"/>} />
                    {
                        paths.map(({path, component: C, props = {}}, index) => {
                            return (
                                <Route key={index} path={path}
                                       element={<C {...props} showNotification={() => NotificationManager}/>}
                                />
                            )
                        })
                    }
                </Routes>
            </Router>
            <div>
                <NotificationContainer/>
            </div>
        </div>
    )
}

