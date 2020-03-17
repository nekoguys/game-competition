import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import DefaultSubmitButton from "../../common/default-submit-button";
import {withRouter} from "react-router-dom";
import isAuthenticated from "../../../helpers/is-authenticated";

class ForbiddenError extends React.Component {

    render() {
        let res;
        if (!isAuthenticated()) {
            res = (
                <div style={{paddingTop: "30px"}}>
                    <div style={{width:"30%", margin: "0 auto"}}>
                        <DefaultSubmitButton onClick={() => {
                                this.props.history.push("/")
                        }} text={"Назад"}/>
                    </div>
                </div>
            )
        }

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "320px", textAlign: "center", fontSize: "36px"}}>
                    <span>
                            Отказано в доступе
                        <br/>
                            403
                    </span>
                </div>
                {res}
            </div>
        );
    }
}

export default withRouter(ForbiddenError);