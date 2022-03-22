import React, {useEffect, useState} from "react";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";
import {useTranslation} from "react-i18next";

import "./messages.css";

export const SelfControlledDefaultToggle = ({expandedAtStart = false, title, children}) => {
    const [expanded, setExpanded] = useState(expandedAtStart);
    return (
        <>
            <DefaultToggle title={title} expanded={expanded} setExpanded={setExpanded}/>
            {expanded ? children : null}
        </>

    )
}

export const DefaultToggle = ({title, expanded, setExpanded, children}) => {
    return (
        <div className={"competition-process-default-toggle-centered-inner-container"}>
            <div className={"competition-process-default-toggle-container"} onClick={() => setExpanded(!expanded)}>
                <div className={"competition-process-default-toggle-text"}>
                    {title}
                </div>
                <img className={"show-settings-toggle-unwind" + (expanded ? " show-settings-toggle-wind" : "")}
                     src={"data:image/svg+xml,%3Csvg width='17' height='13' viewBox='0 0 17 13' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 12L8.69231 2L16 12' stroke='black' stroke-opacity='0.4' stroke-width='1.4359'/%3E%3C/svg%3E%0A"}>
                </img>
                {children}
            </div>
        </div>
    )
}

const MessagesContainer = ({
                               messages, unreadMessagesCount, onReadMessagesCallback = () => {
    }
                           }) => {
    const [expanded, setExpanded] = useState(false)
    const {t} = useTranslation();

    useEffect(() => {
        onReadMessagesCallback();
    }, [expanded])

    let res;
    if (expanded) {
        res = (
            <MessagesListContainer messages={messages}/>
        );
    }
    let badge;
    if (unreadMessagesCount > 0) {
        badge = <span className={"message-container-unread-count"}>{"+" + unreadMessagesCount}</span>;
    }

    return (
        <div>
            <DefaultToggle title={t("competition_process.student.messages.messages")} expanded={expanded}
                           setExpanded={setExpanded}>
                {badge}
            </DefaultToggle>
            {expanded ? <div className={"competition-process-messages-toggle-content-spacer"}/> : null}
            <div className={"competition-process-student-toggle-content"}>
                {res}
            </div>
        </div>
    )
}

export default MessagesContainer;
