import React from "react";
import {useTranslation} from "react-i18next";
import "./language-change.css";

const LanguageChangeComponent = () => {
    const {i18n} = useTranslation('translation');
    const onChangeLanguage = (lang) => {
        window.localStorage.setItem("language", lang);
        i18n.changeLanguage(lang);
    };

    return (
        <div>
            <div className={"language-change-container"}>
                <a href={"#"} onClick={() => onChangeLanguage("ru")}>ru</a>
                |
                <a href={"#"} onClick={() => onChangeLanguage("en")}>en</a>
            </div>
        </div>
    )
}

export default LanguageChangeComponent;
