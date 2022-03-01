import React from "react";
import {withRouter} from "./with-router";
import {isTeacher} from "./role-helper";
import {Navigate} from 'react-router-dom';

function withRedirect(Component, checkFunc = isTeacher) {
    return withRouter(class extends React.Component {
        render() {
            const component = checkFunc() ? <Component {...this.props} /> : <Navigate to={"/forbidden"}/>
            return component;
        }
    })
}

export default withRedirect;