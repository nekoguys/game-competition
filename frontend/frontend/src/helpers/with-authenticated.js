import {withRouter} from "./with-router";
import React, {useEffect} from "react";
import isAuthenticated from "./is-authenticated";
import {useNavigate} from "react-router";
import { Navigate } from 'react-router-dom';

function withAuthenticated(Component, to) {
    return withRouter(class extends React.Component {
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

            return <Navigate to={this.getPath()}/>
        }
    })
}

export function useWithAuthenticatedHook(to) {
    const navigate = useNavigate();

    const getPath = () => {
        if (to) {
            return to;
        } else {
            return "/auth/signin";
        }
    }

    useEffect(() => {
        if (!isAuthenticated()) {
            navigate(getPath())
        }
    })
}

export default withAuthenticated;
