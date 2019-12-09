import React from "react";
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

import "../login-form/login-form.css";

const RegisterForm = ({onEmailChanged = () => {},
                          onPasswordChanged = () => {},
                          onConfPasswordChanged = () => {},
                          onSubmit = () => {}}
                      ) => {

    const formInputStyle = {
        width: "75%",
        borderRadius: "20px",
        lineHeight: "20px",
        padding: "-10px"
    };

    const buttonStyle = {
        marginBottom: "0.7rem"
    };

    return (
        <div className={"login-form form-group "}>
            <DefaultTextInput style={formInputStyle} placeholder={"Почта"} onChange={onEmailChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={"Пароль"} type={"password"} onChange={onPasswordChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={"Повторите пароль"} type={"password"} onChange={onConfPasswordChanged} />
            <DefaultSubmitButton text={"Войти"} style={buttonStyle} onClick={onSubmit} />
        </div>
    )
};

export default RegisterForm;