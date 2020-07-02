import React from "react";
import {withRouter} from "react-router-dom";
import {isTeacher} from "./role-helper";

function withRedirect(Component, checkFunc = isTeacher) {
    return withRouter(class extends React.Component {
        componentDidMount() {
            if (!checkFunc())
                this.props.history.push("/forbidden");
        }

        render() {
            if (checkFunc())
                return <Component {...this.props}/>;

            return <div/>;
        }
    })
}

export default withRedirect;