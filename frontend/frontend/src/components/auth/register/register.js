import React from "react";
import ApiHelper from "../../../helpers/api-helper";
import Header from "../header";
import {withRouter} from "react-router-dom";

import "../login/login.css";
import RegisterForm from "../register-form/register-form";
import showNotification from "../../../helpers/notification-helper";

class Register extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            email: '',
            password: '',
            passwordConf: '',
            isRegButtonDisabled: true
        }
    }

    onEmailChanged = (newEmail) => {
        this.setState({email: newEmail});
    };

    onPasswordChanged = (newPassword) => {
        this.setState(prevState => {
            return {
                password: newPassword,
                isRegButtonDisabled: !(newPassword === prevState.passwordConf
                    && newPassword !== ''
                )
            }
        });
    };

    onPasswordConfChanged = (newConf) => {
        this.setState(prevState => {
            return {
                passwordConf: newConf,
                isRegButtonDisabled: !(newConf === prevState.password
                    && newConf !== ''
                )
            }
        });
    };

    onSubmitClick = () => {
        ApiHelper.signup({password: this.state.password, email: this.state.email}).then(res => {
            if (res.status >= 300) {
                console.log("error in signin request");
                return {success: false, json: res.json()}
            }
            return {success: true, json: res.json()};
        }).then(parsedResponse => {
            console.log(parsedResponse);
            parsedResponse.json.then((value) => {
                console.log({value});
                if (parsedResponse.success) {
                    showNotification(this).success(`Registered Successfully!`, 'Success', 1200);
                } else {
                    if (value.message) {
                        showNotification(this).error(value.message, 'Error', 1200);
                    } else {
                        showNotification(this).error('Error occurred', 'Error', 1200, () => {
                        });
                    }
                }
            })
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
                           isRegButtonDisabled={this.state.isRegButtonDisabled}
                />
            </div>
        )
    }
}

export default withRouter(Register);