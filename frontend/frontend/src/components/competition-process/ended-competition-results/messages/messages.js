import React from "react";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";
import "./messages.css";
import {useTranslation} from "react-i18next";

const ReadonlyMessagesContainer = ({messages}) => {
    const {t} = useTranslation();
    return (
        <>
            <div className={"readonly-messages-list-title-container"}>
                {t('competition_results.messages')}
            </div>
            <MessagesListContainer messages={messages}/>
        </>
    )
}

export default ReadonlyMessagesContainer;
