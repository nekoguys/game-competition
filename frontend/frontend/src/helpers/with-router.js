import {useLocation, useNavigate, useParams} from "react-router";

import React from "react";

export const withRouter = (Component) => {
    const Wrapper = (props) => {
        const history = useNavigate();
        const params = useParams();
        const location = useLocation();
        const match = {params: params};
        return <Component history={history} match={match} location={location} {...props} />;
    };
    return Wrapper;
};