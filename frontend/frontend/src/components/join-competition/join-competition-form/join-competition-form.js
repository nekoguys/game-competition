import React, {useState} from "react";
import DefaultCheckboxButtonGroup from "../../common/default-checkbox-button-group";
import {useTranslation} from "react-i18next";

import "./join-competition-form.css";
import "../../../helpers/common.css";
import GameIdInput from "./new-forms/game-id-input";

class JoinMode {
    static Captain = new JoinMode("captain");
    static Player = new JoinMode("player");

    constructor(name) {
        this.name = name;
    }
}

const JoinCompetitionForm = ({showNotification, pinCheckFetcher, processToCreateTeam, processToJoinTeam}) => {
    const onSubmitGameId = (gameId) => {
        const timeout = 1500;
        pinCheckFetcher(gameId).then(resp => {
            if (resp.exists) {
                showNotification().success("Competition found successfully", "Success", timeout);
                setTimeout(() => {
                    if (joinMode === JoinMode.Captain) {
                        processToCreateTeam(gameId);
                    } else if (joinMode === JoinMode.Player) {
                        processToJoinTeam(gameId);
                    }
                }, timeout)
            } else {
                showNotification().error(`Competition with id: ${gameId} not found`, "Error", timeout);
            }
        })
    }

    const {t} = useTranslation();

    const [joinMode, setJoinMode] = useState(JoinMode.Player);

    return (
        <div>
            <div className={"page-title form-title"}>
                Войти в игру
            </div>
        <div style={{display: "flex"}}>

            <div className={"mode-switcher-container"}>
                <DefaultCheckboxButtonGroup
                    initialChoiceIndex={[JoinMode.Captain, JoinMode.Player].indexOf(joinMode)}
                    choices={[t('join_competition.join.captain'), t('join_competition.join.member')]}
                    buttonClasses={" mode-switcher__button-element"}
                    style={{width: "100%"}}
                    onChoiceChanged={[
                        () => setJoinMode(JoinMode.Captain),
                        () => setJoinMode(JoinMode.Player)
                    ]}/>
            </div>
        </div>
            <div>
                <GameIdInput onSubmit={onSubmitGameId}/>
            </div>
        </div>
    )
}

export default JoinCompetitionForm;