import React, {useState} from "react";

import "./new-join-competition-captain-form.css";
import "../../../../../helpers/common.css";
import {useTranslation} from "react-i18next";
import {useNavigate, useParams} from "react-router";
import DefaultSubmitButton from "../../../../common/default-submit-button";
import {NavbarHeaderWithFetcher} from "../../../../app/app";


const NewJoinCompetitionCaptainForm = ({fetchers, captainEmailProvider, showNotification}) => {
    const {t} = useTranslation();
    const matchParams = useParams();
    const {pin} = matchParams;
    const navigate = useNavigate();

    const [form, setForm] = useState({teamName: "", password: ""})
    const createButtonDisabled = !(form.teamName.length >= 4 && form.password.length >= 4)

    const createTeam = () => {
        const captainEmail = captainEmailProvider.get();
        const teamCreationDTO = {
            captain_email: captainEmail,
            team_name: form.teamName,
            password: form.password
        };
        const notificationTimeout = 2000;
        fetchers.createTeam(pin, teamCreationDTO)
            .then(_ => {
                showNotification().success("Team created successfully", "Success", notificationTimeout);
                navigate("/competitions/waiting_room/" + pin)
            })
            .catch(_ => {
                showNotification().error("Encountered Error", "Error", notificationTimeout)
            })
    }

    return (
        <div>
            <div>
                <NavbarHeaderWithFetcher/>
            </div>
            <div className={"below-navbar"}>
                <div className={"page-title create-team-form-title"}>
                    {t("join_competition.captain.enter_game_with_id") + ` №${pin}`}
                </div>
                <div className={"create-team-form-container"}>
                    <input className={"create-team-form__input"} value={form.teamName} placeholder={t("join_competition.captain.team_name")} onChange={(event) => setForm({...form, teamName: event.target.value})}/>
                    <input className={"create-team-form__input"} value={form.password} placeholder={t("join_competition.captain.password_short")} onChange={(event) => setForm({...form, password: event.target.value})}/>
                    <div className={"create-team-button-container"}>
                        <DefaultSubmitButton
                            text={t("join_competition.captain.enter") }
                            additionalClasses={["create-team-button"]}
                            isDisabled={createButtonDisabled}
                            onClick={createTeam}
                        />
                    </div>
                </div>
            </div>
        </div>
    )
}

export default NewJoinCompetitionCaptainForm;
