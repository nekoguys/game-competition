import React, {useEffect, useState} from "react";
import Header from "../header";
import LoginForm from "../login-form";
import 'react-notifications/lib/notifications.css';
import {useTranslation} from "react-i18next";

import "./login.css"
import LanguageChangeComponent from "../language-change/language-change";
import {useNavigate} from "react-router";

const Login = ({fetchers, onSuccessfulLogin, authProvider, showNotification}) => {
    const [form, setForm] = useState({email: '', password: ''});
    const { t } = useTranslation('translation')
    const navigate = useNavigate();

    const changeEmail = (newEmail) => {
        setForm({...form, email: newEmail})
    };
    const changePassword = (newPassword) => {
        setForm({...form, password: newPassword});
    };

    useEffect(() => {
        if (authProvider.isAuthenticated()) {
            navigate('/competitions/history');
        }
    });

    const submit = () => {
        fetchers.submit(form)
            .then(resp => {
                onSuccessfulLogin(resp);
                const timeout = 1200;
                showNotification().success(`Welcome, ${resp.email}!`, 'Success', timeout);
                setTimeout(() => {
                    navigate('/competitions/history');
                    console.log("expect redirect to competitions history");
                }, timeout);
            })
            .catch((error) => {
                showNotification().error(error.message || "Error occurred", 'Error', 1200);
            })
    }

    return (
        <div>
            <div className={"container login-group"}>
                <div className={"d-flex headers"}>
                    <Header text={t("auth.login.enter")} style={{marginRight: "20px"}} isSelected={true}/>
                    <Header text={t("auth.login.register")} isSelected={false} onClick={() => navigate("/auth/signup")} />
                </div>
                <LoginForm onEmailChanged={changeEmail}
                           onPasswordChanged={changePassword}
                           onSubmit={submit}
                />
            </div>
            <LanguageChangeComponent/>
        </div>
    )
};

export default Login;