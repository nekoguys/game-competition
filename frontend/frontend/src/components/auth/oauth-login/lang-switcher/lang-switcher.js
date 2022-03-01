import React, {useState} from "react";
import {Language} from "../../../../helpers/localization-helper";

import "./lang-switcher.css";

const LangSwitcher = ({currentLanguage = Language.Russian, changeLanguage = (_) => {}}) => {
    const langs = [Language.Russian, Language.English];
    const langIndex = langs.indexOf(currentLanguage);
    const [selectedIndex, setSelectedIndex] = useState(langIndex);
    const [ruClasses, enClasses] = [containerClassNamesFor(0, selectedIndex), containerClassNamesFor(1, selectedIndex)];

    return (
        <div className="switch-container">
            <div className={ruClasses[0]} onClick={() => {
                setSelectedIndex(0);
                changeLanguage(langs[0]);
            }}>
                <p className={ruClasses[1]}>ru</p>
            </div>
            <div className={enClasses[0]} onClick={() => {
                setSelectedIndex(1);
                changeLanguage(langs[1]);
            }}>
                <p className={enClasses[1]}>en</p>
            </div>
        </div>
    )
}

const containerClassNamesFor = (index, selectedIndex) => {
    let containerResultClassName = "switch-option-container";
    let textResultClassName = "switch-option-text";

    if (index === selectedIndex) {
        containerResultClassName += " switch-option-container-selected";
        textResultClassName += " switch-option-text-selected"
    }
    return [containerResultClassName, textResultClassName];
}

export default LangSwitcher;