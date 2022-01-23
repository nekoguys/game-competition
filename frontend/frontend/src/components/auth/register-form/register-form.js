import React from "react";
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

import "../login-form/login-form.css";
import {useTranslation} from "react-i18next";

const RegisterForm = ({
        isRegButtonDisabled = false,
        onEmailChanged = () => {},
        onPasswordChanged = () => {},
        onConfPasswordChanged = () => {},
        onSubmit = () => {}
    }) => {

    const formInputStyle = {
        width: "75%",
        borderRadius: "20px",
        lineHeight: "25px",
        padding: "-10px"
    };

    const buttonStyle = {
        marginBottom: "0.7rem"
    };

    const { t } = useTranslation();

    return (
        <div className={"login-form form-group "}>
            <DefaultTextInput style={formInputStyle} placeholder={t('auth.login.mail')} onChange={onEmailChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={t('auth.login.password')} type={"password"} onChange={onPasswordChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={t('auth.register.repeat_password')} type={"password"} onChange={onConfPasswordChanged} />
            <DefaultSubmitButton text={t('auth.register.button')} style={buttonStyle} onClick={onSubmit} isDisabled={isRegButtonDisabled} />
        </div>
    )
};

export default RegisterForm;