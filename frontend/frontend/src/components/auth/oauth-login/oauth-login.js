import React from "react";

import logoImage from "./logo.png";

import "./oauth-login.css";
import LangSwitcher from "./lang-switcher";
import {useTranslation} from "react-i18next";

const OAuthLogin = ({localizationHelper}) => {
    const { t, i18n } = useTranslation();
    const greetText = t("auth.oauth.greeting");
    return (
        <div>
            <div className={"logo-container"}>
                <img src={logoImage}/>
            </div>
            <div className="greeting-title-container">
                <p className="greeting-title">
                    {greetText}
                </p>
            </div>
            <div className="sign-button-container">
                <button className="microsoft-signin-button"> Sign in with Microsoft</button>
            </div>
            <div className="language-switch-container">
                <LangSwitcher
                    currentLanguage={localizationHelper.language}
                    changeLanguage={(lang) => {
                        localizationHelper.setLanguage(lang);
                        i18n.changeLanguage(lang.name);
                    }}
                />
            </div>
        </div>
    )
}

export default OAuthLogin;