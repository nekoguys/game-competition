import React from "react";
import {BrowserRouter as Router, Redirect, Route, Switch,} from "react-router-dom";
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


const paths = [
    {
        path: "/auth/signin",
        component: Login
    },
    {
        path: "/auth/signup",
        component: Register
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
    }
];

export default class App extends React.Component{

    showNotification = () => {
        return NotificationManager;
    };

    render() {
        return (
            <div>
            <Router>
                <Switch>
                    <Redirect exact from="/" to="/auth/signin" />
                    {
                        paths.map(({path, component: C}, index) => {
                            return (
                                <Route key={index} path={path}
                                       render={(props) => <C {...props} showNotification={this.showNotification}/>}
                                />
                            )
                        })
                    }
                </Switch>
            </Router>
                <div>
                    <NotificationContainer/>
                </div>
            </div>
        )
    }
}