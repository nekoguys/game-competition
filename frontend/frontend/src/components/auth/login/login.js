import React from "react";
import Header from "../header";
import LoginForm from "../login-form";
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";
import 'react-notifications/lib/notifications.css';


import "./login.css"
import showNotification from "../../../helpers/notification-helper";

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
            console.log(res);
            if (res.status >= 300) {
                console.log("error in signin request");
                return {success: false, json: res.json()}
            }
            return {success: true, json: res.json()};
        }).then(parsedResponse => {
            console.log(parsedResponse);
            parsedResponse.json.then((value) => {
                console.log({value});
                const timeout = 1200;

                if (parsedResponse.success) {
                    window.localStorage.setItem("accessToken", value.accessToken);
                    window.localStorage.setItem("user_email", value.email);
                    window.localStorage.setItem("roles", value.authorities.map(el => el.authority));
                    showNotification(this).success(`Welcome, ${value.email}!`, 'Success', timeout);
                    setTimeout(() => {
                        this.props.history.push('/competitions/history');
                        console.log("expect redirect to competitions history");
                    }, timeout);

                } else {
                    if (value.message) {
                        showNotification(this).error(value.message, 'Error', timeout);
                    } else {
                        showNotification(this).error('Error occurred', 'Error', timeout, () => {
                        });
                    }
                }
            });
        });
    };

    componentDidMount() {
        if (isAuthenticated()) {
            this.props.history.push('/competitions/history');
        }
    }

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