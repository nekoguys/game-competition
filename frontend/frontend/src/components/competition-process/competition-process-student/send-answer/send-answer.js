import React from "react";

import "./send-answer.css";
import {useTranslation} from "react-i18next";
import {
    TextInputWithSubmitButton
} from "../../../join-competition/join-competition-player-form/text-input-with-submit-button/text-input-with-submit-button";

const SendAnswerNew = ({onSubmit}) => {
    const {t} = useTranslation();

    return (
        <div className={"competition-process-student-answer-form__input-container"}>
            <TextInputWithSubmitButton clearOnSubmit={true}
                                       placeholder={t('competition_process.student.send_answer.send_answer')}
                                       onSubmit={onSubmit}
                                       buttonStyle={{}} buttonClasses={" game-id-form__input-button"} inputStyle={{}}
                                       inputClasses={" competition-process-student-answer-form__input-input"}
                                       submitOnKey={'enter'}
                                       imgClasses={" competition-process-student-answer-form__input-img"}
            />
        </div>
    )
}

export default SendAnswerNew;
