import React from "react";
import Header from "../header";
import LoginForm from "../login-form";
import {withRouter} from "../../../helpers/with-router";
import ApiHelper from "../../../helpers/api-helper";
import 'react-notifications/lib/notifications.css';
import {withTranslation} from "react-i18next";

import "./login.css"
import showNotification from "../../../helpers/notification-helper";
import isAuthenticated from "../../../helpers/is-authenticated";
import LanguageChangeComponent from "../language-change/language-change";

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
                    window.localStorage.setItem("expirationTimestamp", value.expirationTimestamp);
                    
                    showNotification(this).success(`Welcome, ${value.email}!`, 'Success', timeout);
                    setTimeout(() => {
                        this.props.history('/competitions/history');
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
            this.props.history('/competitions/history');
        }
    }

    onHeaderRegisterClick = () => {
        this.props.history('/auth/signup');
    };

    render() {

        const headerStyle = {
            marginRight: "20px"
        };

        return (
            <div>
            <div className={"container login-group"}>
                <div className={"d-flex headers"}>
                    <Header text={this.props.i18n.t("auth.login.enter")} style={headerStyle} isSelected={true}/>
                    <Header text={this.props.i18n.t("auth.login.register")} isSelected={false} onClick={this.onHeaderRegisterClick} />
                </div>
                <LoginForm onEmailChanged={this.onEmailChanged}
                           onPasswordChanged={this.onPasswordChanged}
                           onSubmit={this.onSubmitClick}
                />
            </div>
                <LanguageChangeComponent/>
            </div>
        )
    }
}

export default withTranslation('translation')(withRouter(Login));