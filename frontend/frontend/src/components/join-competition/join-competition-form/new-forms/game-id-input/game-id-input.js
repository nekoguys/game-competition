import React from "react";

import "./game-id-input.css";
import {
    TextInputWithSubmitButton
} from "../../../join-competition-player-form/text-input-with-submit-button/text-input-with-submit-button";
import submitButtonImage from "./submitButton.svg";

import {useTranslation} from "react-i18next";

const GameIdInput = ({onSubmit}) => {
    const {t} = useTranslation();

    return (
        <div style={{marginTop: "18px"}}>
        <div className={"game-id-form__input-container"}>
            <TextInputWithSubmitButton imagePath={submitButtonImage} containerStyle={{margin: "0 auto"}}
                                       placeholder={t('join_competition.captain.game')} onSubmit={onSubmit}
                                       buttonStyle={{}} buttonClasses={" game-id-form__input-button"} inputStyle={{}}
                                       inputClasses={" game-id-form__input-input"}
                                       imgClasses={" game-id-form__input-img"}
            />
        </div>
        </div>
    )
}

export default GameIdInput;
