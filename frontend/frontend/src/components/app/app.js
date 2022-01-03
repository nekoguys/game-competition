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


const paths = [
    {
        path: "/auth/signin",
        component: Login
    },
    {
        path: "/auth/signup",
        component: Register,
        props: {
            fetchers: {
                submit: (params) => { return apiFetcher(params, (credentials) => { return ApiHelper.signup(credentials) }) }
            }
        }
    },
    {
        path: "/competitions/history",
        component: CompetitionHistory
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
        component: Verification
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

export default class App extends React.Component{

    render() {
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
}