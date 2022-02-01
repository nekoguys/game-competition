import React from "react";
import {BrowserRouter as Router, Navigate, Route, Routes} from "react-router-dom";
import Login from "../auth/login";
import Register from "../auth/register/register";
import CompetitionHistory from "../competition-history/competition-history";
import CreateCompetition from "../create-competition/create-competition";
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

const signinFetcher = {
    mock: (_) => {
        return new Promise(resolve =>
            resolve({accessToken: "1", email: "someEmail", authorities: ['Student'], expirationTimestamp: getUTCSeconds(new Date(Date.now() + 24*60*60*1000))})
        )
    },
    real: (params) => { return apiFetcher(params, (credentials) => ApiHelper.signin(credentials)) }
}["real"];

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

const historyFetcher = {
    mock: (_) => {
        return new Promise(resolve => setTimeout(() => {
            resolve([
                {
                    name: "Конкуренция на рынке пшеницы",
                    state: "Registration",
                    pin: "1234",
                    owned: false
                },
                {
                    name: "Ко",
                    state: "Registration",
                    pin: "1234",
                    owned: false
                }
            ])
        }))
    },
    real: (start, count) => { return apiFetcher([start, count], (params) => ApiHelper.competitionsHistory(params[0], params[1])) }
}["real"]

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
                history: historyFetcher
            }
        }
    },
    {
        path: "/competitions/create",
        component: CreateCompetition
    },
    {
        path: "/competitions/draft_competition/:pin",
        component: CreateCompetition
    },
    {
        path: "/competitions/join",
        component: JoinCompetition
    },
    {
        path: "/competitions/after_registration_opened/:pin",
        component: AfterRegistrationOpenedComponent
    },
    {
        path: "/competitions/waiting_room/:pin",
        component: WaitingRoom
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