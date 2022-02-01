import React from 'react';
import "./login-form.css"
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

import {useTranslation} from 'react-i18next';

const LoginForm = ({onEmailChanged = () => {}, onPasswordChanged = () => {}, onSubmit = () => {}}) => {

    const formInputStyle = {
        width: "75%",
        borderRadius: "20px",
        lineHeight: "25px",
        padding: "-10px"
    };

    const { t } = useTranslation();

    const buttonStyle = {
        marginBottom: "0.7rem"
    };

    return (
        <div className={"login-form form-group "}>
            <DefaultTextInput style={formInputStyle} placeholder={t('auth.login.mail')} onChange={onEmailChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={t('auth.login.password')} type={"password"} onChange={onPasswordChanged} />
            <div className={'register-button-container'}>
                <DefaultSubmitButton text={t('auth.login.button')} style={buttonStyle} additionalClasses={['register-button']} onClick={onSubmit} />
            </div>
        </div>
    )
};

export default LoginForm;