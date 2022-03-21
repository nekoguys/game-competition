import React from "react";

import "./change-round-length-container.css";
import submitButtonImage from "../../../join-competition/join-competition-player-form/submitButton.png";
import {useTranslation, withTranslation} from "react-i18next";
import {
    TextInputWithSubmitButton
} from "../../../join-competition/join-competition-player-form/text-input-with-submit-button/text-input-with-submit-button";

const ChangeRoundLengthInput = ({onSubmit}) => {
    const {t} = useTranslation();
    return (
        <div>
            <div className={"current-round-length__input-container"}>
                <TextInputWithSubmitButton imagePath={submitButtonImage}
                                           placeholder={t('competition_process.teacher.round_length.change_length')}
                                           onSubmit={onSubmit}
                                           buttonStyle={{}} buttonClasses={" game-id-form__input-button"}
                                           inputStyle={{}}
                                           inputClasses={" game-id-form__input-input"}
                                           imgClasses={" game-id-form__input-img"}
                />
            </div>
        </div>
    )
}

class ChangeRoundLengthContainer extends React.Component {
    render() {
        const {i18n} = this.props;
        return (
            <div style={{margin: "0 auto"}}>
                <CurrentRoundLengthContainer i18n={i18n} currentRoundLength={this.props.currentRoundLength}/>
                <ChangeRoundLengthInput onSubmit={this.props.changeRoundLengthCallback}/>
            </div>
        )

    }
}

class CurrentRoundLengthContainer extends React.Component {
    render() {
        const {currentRoundLength} = this.props;
        const message = `${this.props.i18n.t("competition_process.teacher.round_length.current_length")} ${currentRoundLength}`;
        return (
            <div className={"current-round-length-container"}>
                {message}
            </div>
        )
    }
}

export default withTranslation('translation')(ChangeRoundLengthContainer);
