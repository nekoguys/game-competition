import React from 'react';
import "./login-form.css"
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

const LoginForm = ({onEmailChanged = () => {}, onPasswordChanged = () => {}, onSubmit = () => {}}) => {

    const formInputStyle = {
        width: "75%",
        borderRadius: "20px",
        lineHeight: "25px",
        padding: "-10px"
    };

    const buttonStyle = {
        marginBottom: "0.7rem"
    };

    return (
        <div className={"login-form form-group "}>
            <DefaultTextInput style={formInputStyle} placeholder={"Почта"} onChange={onEmailChanged} />
            <DefaultTextInput style={formInputStyle} placeholder={"Пароль"} type={"password"} onChange={onPasswordChanged} />
            <DefaultSubmitButton text={"Войти"} style={buttonStyle} onClick={onSubmit} />
        </div>
    )
};

export default LoginForm;