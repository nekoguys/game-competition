import React from "react";
import ApiHelper from "../../../helpers/api-helper";
import Header from "../header";
import {withRouter} from "react-router-dom";

import "../login/login.css";
import RegisterForm from "../register-form/register-form";

class Register extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            email: '',
            password: '',
            passwordConf: '',
        }
    }

    onEmailChanged = (newEmail) => {
        this.setState({email: newEmail});
    };

    onPasswordChanged = (newPassword) => {
        this.setState({password: newPassword});
    };

    onPasswordConfChanged = (newConf) => {
        this.setState({passwordConf: newConf});
    };

    onSubmitClick = () => {
        ApiHelper.signup({password: this.state.password, email: this.state.email}).then(res => {
            if (res.status >= 300) {
                console.log("error in signin request");
                return {}
            }
            return res.json();
        }).then(parsedResponse => {
            console.log(parsedResponse);
        })
    };

    onLoginHeaderClick = () => {
        this.props.history.push('/auth/signin')
    };

    render() {
        const headerStyle = {
            marginRight: "20px"
        };

        return (
            <div className={"container login-group"}>
                <div className={"d-flex headers"}>
                    <Header text={"Войти"} style={headerStyle} isSelected={false} onClick={this.onLoginHeaderClick}/>
                    <Header text={"Регистрация"} isSelected={true}/>
                </div>
                <RegisterForm onEmailChanged={this.onEmailChanged}
                           onPasswordChanged={this.onPasswordChanged}
                           onConfPasswordChanged={this.onPasswordConfChanged}
                           onSubmit={this.onSubmitClick}
                />
            </div>
        )
    }
}

export default withRouter(Register);