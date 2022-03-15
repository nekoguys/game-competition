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
import {LocalStorageWrapper} from "../../helpers/storage-wrapper";
import CreateCompetition from "../create-competition/create-competition";
import EventSourceMock from "../../helpers/mocks/event-source-mock";
import EventSourceWrapper from "../../helpers/event-source-wrapper";
import {isTeacher} from "../../helpers/role-helper";

const signinFetcher = {
    mock: (_) => {
        return new Promise(resolve =>
            resolve({accessToken: "1", email: "someEmail", authorities: ['Student'], expirationTimestamp: getUTCSeconds(new Date(Date.now() + 24*60*60*1000))})
        )
    },
    real: (params) => { return apiFetcher(params, (credentials) => ApiHelper.signin(credentials)) }
}["mock"];

const signupFetcher = {
    mock: (_) => {
        return new Promise(resolve =>
            resolve({})
        )
    },
    real: (params) => { return apiFetcher(params, (credentials) => { return ApiHelper.signup(credentials) }) }
}["real"];

const verificationFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({message: "Подтвержден"})
        }, 2500) )
    },
    real: (token) => { return apiFetcher(token, (token) => ApiHelper.accountVerification(token)) }
}["real"];

const userInfoFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({userDescription: "Иванов И.И."})
        }))
    },
    real: () => { return apiFetcher({}, (_) => ApiHelper.getNavBarInfo()) }
}["mock"]

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
                    owned: true
                },
                {
                    name: "Ко",
                    state: "InProcess",
                    pin: "12345",
                    owned: true
                }
            ])
        }))
    },
    real: (start, count) => { return apiFetcher([start, count], (params) => ApiHelper.competitionsHistory(params[0], params[1])) }
}["mock"]

const pinFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({exists: true})
        }))
    },
    real: (pin) => { return apiFetcher(pin, (pin) => ApiHelper.checkPin(pin)) }
}["mock"]

const createTeamFetcher = {
    mock: (pin, _) => {
        return new Promise(resolve => setTimeout(() => {
            resolve({})
        }))
    },
    real: (data) => { return apiFetcher(data, (teamData) => ApiHelper.createTeam(teamData)) }
}["mock"]

const joinTeamFetcher = {
    mock: (_, {teamName}) => {
        return new Promise(resolve => resolve({currentTeamName: teamName}))
    },
    real: (data) => { return apiFetcher(data, (data) => ApiHelper.joinTeam(data)) }
}['mock']

const createCompetitionFetcher = {
    mock: (_) => {
        return new Promise(resolve => resolve({pin: 1234}))
    },
    real: (obj) => {
        return apiFetcher(obj, (data) => ApiHelper.createCompetition(data))
    }
}['mock'];

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
}['mock'];

const startCompetitionFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => resolve({}), 600))
    },
    real: (pin) => {
        return apiFetcher(pin, (pin) => ApiHelper.startCompetition(pin))
    }
}['mock']

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
}['mock']

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
}['mock']


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
}['mock']

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
}['mock']

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
            }
        ], 200)
    },
    real: (pin) => {
        return new EventSourceWrapper(ApiHelper.teamCreationEventSource(pin));
    }
}['mock']

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
        component: CompetitionProcessStudentRoot
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