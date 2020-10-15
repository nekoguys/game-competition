import React from "react";
import {withTranslation} from "react-i18next";
import "./language-change.css";

class LanguageChangeComponent extends React.Component {

    onChangeLanguage = (lang) => {
        window.localStorage.setItem("language", lang);
        this.props.i18n.changeLanguage(lang);
    };

    render() {
        return (
            <div>
            <div className={"language-change-container"}>
                <a href={"#"} onClick={() => this.onChangeLanguage("ru")}>ru</a>
                |
                <a href={"#"} onClick={() => this.onChangeLanguage("en")}>en</a>
            </div>
            </div>
        )
    }
}

export default withTranslation('translation')(LanguageChangeComponent)
