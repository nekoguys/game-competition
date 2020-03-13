import {withRouter} from "react-router-dom";
import React from "react";
import isAuthenticated from "./is-authenticated";

function withAuthenticated(Component, to) {
    return withRouter(class extends React.Component {
        componentDidMount() {
            if (!isAuthenticated()) {
                this.props.history.push(this.getPath());
            }
        }

        getPath() {
            if (to) {
                return to;
            } else {
                return "/auth/signin";
            }
        }

        render() {
            if (isAuthenticated())
                return <Component {...this.props}/>;

            return <div/>;
        }
    })
};

export default withAuthenticated;
