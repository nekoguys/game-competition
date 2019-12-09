import React from "react";
import Header from "../header";
import LoginForm from "../login-form";
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";


import "./login.css"

class Login extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            email: '',
            password: '',
        }
    }

    onEmailChanged = (newEmail) => {
        this.setState({email: newEmail});
    };

    onPasswordChanged = (newPassword) => {
        this.setState({password: newPassword});
    };

    onSubmitClick = () => {
        ApiHelper.signin(this.state).then(res => {
            if (res.status >= 300) {
                console.log("error in signin request");
                return {}
            }
            return res.json();
        }).then(parsedResponse => {
            console.log(parsedResponse);
        })
    };

    onHeaderRegisterClick = () => {
        this.props.history.push('/auth/signup');
    };

    render() {

        const headerStyle = {
            marginRight: "20px"
        };

        return (
            <div className={"container login-group"}>
                <div className={"d-flex headers"}>
                    <Header text={"Войти"} style={headerStyle} isSelected={true}/>
                    <Header text={"Регистрация"} isSelected={false} onClick={this.onHeaderRegisterClick} />
                </div>
                <LoginForm onEmailChanged={this.onEmailChanged}
                           onPasswordChanged={this.onPasswordChanged}
                           onSubmit={this.onSubmitClick}
                />
            </div>
        )
    }
}

export default withRouter(Login);