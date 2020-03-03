import React from "react";
import {withRouter} from "react-router-dom";

class RedirectWrapper extends React.Component {

    componentDidMount() {
        if (window.localStorage.getItem("roles").length === 1)
            this.props.history.push(this.props.uri)
    }

    render() {
        return (
            <div>
                {this.props.children}
            </div>
        );
    }
}

export default withRouter(RedirectWrapper);