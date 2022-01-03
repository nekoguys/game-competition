import React, {useState} from "react";
import Header from "../header";
import {useTranslation} from "react-i18next";

import "../login/login.css";
import RegisterForm from "../register-form/register-form";
import LanguageChangeComponent from "../language-change/language-change";
import {useNavigate} from "react-router";

const Register = ({fetchers, showNotification}) => {
    const { t } = useTranslation('translation');
    const navigate = useNavigate();

    const [form, setForm] = useState({
        password: "",
        email: "",
        passwordConf: ""
    });

    const isButtonEnabled = () => {
        return form.password === form.passwordConf &&
            form.email.length > 0 && form.password.length > 0
    }

    const onFormChanged = (name, newEmail) => {
        setForm({...form, [name]: newEmail});
    }

    const onSubmit = () => {
        fetchers.submit({password: form.password, email: form.email})
            .then(() => {
                showNotification().success(`Registered Successfully!`, 'Success', 1200);
            })
            .catch((error) => {
                showNotification().error(error.message, 'Error', 1200);
            })
    }

    return (
        <div>
            <div className={"container login-group"}>
                <div className={"d-flex headers"}>
                    <Header style={{marginRight: "20px"}} text={t("auth.login.enter")} isSelected={false} onClick={() => { navigate("/auth/signin") }}/>
                    <Header text={t("auth.login.register")} isSelected={true}/>
                </div>
                <RegisterForm
                    onEmailChanged={(email) => { onFormChanged("email", email) }}
                    onPasswordChanged={(password) => { onFormChanged("password", password) }}
                    onConfPasswordChanged={(passwordConf) => { onFormChanged("passwordConf", passwordConf) }}
                    onSubmit={onSubmit}
                    isRegButtonDisabled={isButtonEnabled()}
                />
            </div>
            <div><LanguageChangeComponent/></div>
        </div>
    )
}

export default Register;