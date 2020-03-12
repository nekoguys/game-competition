import React from "react";
import {withRouter} from "react-router-dom";
import isTeacher from "./role-helper";

function withRedirect(Component) {
    return withRouter(class extends React.Component {

        componentDidMount() {
            if (!isTeacher())
                this.props.history.push("/forbidden");
        }

        render() {
            if (isTeacher())
                return <Component {...this.props}/>;

            return <div/>;
        }
    })
}

export default withRedirect;