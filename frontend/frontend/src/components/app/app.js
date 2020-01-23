import React from "react";
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect,
} from "react-router-dom";
import Login from "../auth/login";
import Register from "../auth/register/register";
import CompetitionHistory from "../competition-history/competition-history";
import CreateCompetition from "../create-competition/create-competition";
import JoinCompetition from "../join-competition/join-competition";

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
                </Switch>
            </Router>
        )
    }
}