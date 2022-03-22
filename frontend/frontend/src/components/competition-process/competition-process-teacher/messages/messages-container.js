import React from "react";
import submitButtonImage from "../../../join-competition/join-competition-player-form/submitButton.png";
import "./messages-container.css";
import {useTranslation, withTranslation} from "react-i18next";
import {
    TextInputWithSubmitButton
} from "../../../join-competition/join-competition-player-form/text-input-with-submit-button/text-input-with-submit-button";

const SendMessageContainer = ({onSubmit}) => {
    const {t} = useTranslation();
    return (
        <div>
            <div className={"current-round-length__input-container"}>
                <TextInputWithSubmitButton imagePath={submitButtonImage}
                                           placeholder={t('competition_process.teacher.messages.message')}
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

class MessagesContainer extends React.Component {
    render() {
        return (
            <div>
                <SendMessageContainer onSubmit={this.props.sendMessageCallBack}/>
                <MessagesListContainer messages={this.props.messages}/>
            </div>
        )
    }
}

export class MessagesListContainer extends React.Component {
    render() {
        const {messages = []} = this.props;
        return (
            <div>
                {messages.map(el => {
                    return <div key={el.dateStr + el.message} className={"competition-process-message-container-root"}>
                        <SingleMessageContainer message={el}/></div>
                })}
            </div>
        )
    }
}

class SingleMessageContainer extends React.Component {
    render() {
        const {message} = this.props;
        return (
            <div className={"competition-process-teacher-message-style"}>
                <div className={"competition-process-single-message-container"}>
                    <div className={"competition-process-single-message-text"}>
                        {message.message}
                    </div>
                    <div className={"competition-process-single-message-date-text"}>
                        {message.dateStr}
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(MessagesContainer);
