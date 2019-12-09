import React from "react";
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Redirect,
} from "react-router-dom";
import Login from "../auth/login";
import Register from "../auth/register/register";

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
                </Switch>
            </Router>

        )
    }
}