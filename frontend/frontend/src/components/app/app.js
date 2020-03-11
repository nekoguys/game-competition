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
import CompetitionProcessStudentRoot from "../competition-process/competition-process-student/root";
import Verification from "../auth/verification/verification";
import EndedCompetitionResultsRoot from "../competition-process/ended-competition-results/root";
import UserProfileRoot from "../user-profile/root";

export default class App extends React.Component{
    render() {
        return (
            <Router>
                <Switch>
                    <Redirect exact from="/" to="/auth/signin" />
                    <Route path={"/auth/signin"}>
                        <Login />
                    </Route>
                    <Route path={"/auth/signup"}>
                        <Register />
                    </Route>
                    <Route path={"/competitions/history"}>
                        <CompetitionHistory/>
                    </Route>
                    <Route path={"/competitions/create"}>
                        <CreateCompetition/>
                    </Route>
                    <Route path={"/competitions/join"}>
                        <JoinCompetition/>
                    </Route>
                    <Route path={"/competitions/after_registration_opened/:pin"} component={AfterRegistrationOpenedComponent}/>
                    
                    <Route path={"/competitions/waiting_room/:pin"} component={WaitingRoom} />

                    <Route path={"/competitions/process_teacher/:pin"} component={CompetitionProcessTeacherRootComponent} />

                    <Route path={"/competitions/process_captain/:pin"} component={CompetitionProcessStudentRoot}/>

                    <Route path={"/auth/verification/:token"} component={Verification}/>

                    <Route path={"/competitions/results/:pin"} component={EndedCompetitionResultsRoot}/>

                    <Route path={"/profile/"} component={UserProfileRoot}/>
                </Switch>
            </Router>
        )
    }
}