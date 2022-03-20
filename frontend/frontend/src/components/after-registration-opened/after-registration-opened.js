import React, {useEffect, useState} from "react";
import "./after-registration-opened.css";
import CompetitionParamsForm from "../create-competition/competition-params";
import {toCompetitionFormJsonObject} from "../../helpers/competition-params-helper";
import withRedirect from "../../helpers/redirect-helper";
import {useTranslation} from "react-i18next";
import {useNavigate, useParams} from "react-router";
import {NavbarHeaderWithFetcher} from "../app/app";
import ShowSettingsToggle from "./internal/show-settings-toggle";
import NewTeamCollection
    from "../join-competition/join-competition-form/new-forms/new-join-competition-member-form/team-collection";


const AfterRegistrationOpenedNewComponent = ({eventSources, fetchers, showNotification}) => {
    const params = useParams();
    const navigate = useNavigate()
    const {t} = useTranslation();
    const [teams, setTeams] = useState([]);
    const [expanded, setExpanded] = useState(false);
    const [formState, setFormState] = useState({});
    const {pin} = params;

    useEffect(() => {
        const eventSource = eventSources.teams(pin);
        eventSource.subscribe((newTeam) => {
            console.log({newTeam, teams});
            setTeams(prevValue => {
                const teamsCopy = prevValue;
                const teamIndex = teamsCopy.map(el => el.idInGame).indexOf(newTeam.idInGame);
                if (teamIndex === -1) {
                    return [...prevValue, newTeam];
                } else {
                    teamsCopy[teamIndex] = newTeam;
                    return teamsCopy;
                }
            });
        })

        return function cleanup() {
            eventSource.close();
        }
    }, [])

    useEffect(() => {
        fetchers.cloneInfo(pin).then(resp => {
            setFormState(resp);
        })
    }, [])

    const startCompetition = () => {
        const {pin} = params;

        fetchers.startCompetition(pin).then(_ => {
            showNotification().success("Competition Started!", "Success", 1500);
            setTimeout(() => {
                navigate("/competitions/process_teacher/" + pin)
            }, 1500);
        }).catch(err => {
            showNotification().error(err.message || "Error", "Error", 1500)
        })
    }

    const updateCompetition = () => {
        const {pin} = params;
        fetchers.updateCompetition(pin, toCompetitionFormJsonObject(formState)).then(resp => {
            setFormState(resp);
            showNotification().success("Competition updated successfully", "Success", 900);
        })
    }

    const paramsForm = expanded
        ? <CompetitionsParamsWrapper formState={formState} setFormState={setFormState} saveAction={() => updateCompetition()}/>
        : null

    return (
        <div>
            <div>
                <NavbarHeaderWithFetcher/>
            </div>
            <div className={"below-navbar"}>
                <div className={"after-registration-opened-title-container"}>
                    <div className={"after-registration-opened-title"}>{t('waiting_room_new.registration_opened')}</div>
                    <div className={"after-registration-opened-subtitle"}>{t('waiting_room_new.create') + pin}</div>
                </div>
                <div>
                    <div className={"after-registration-opened-show-settings-toggle"}>
                        <ShowSettingsToggle t={t} isExpanded={expanded} setExpanded={setExpanded}/>
                    </div>
                    {paramsForm}
                </div>
                <div className={"after-registration-opened-params-teams-spacer"}>

                </div>
                <div className={"after-registration-opened-teams-container"}>
                    <div className={"after-registration-opened-teams-container-title"}>
                        {t("waiting_room_new.teams_list_title")}
                    </div>
                    <div className={"after-registration-opened-teams-inner-container"}>
                        <NewTeamCollection readOnly={true} teams={teams}/>
                    </div>

                    <div className={"after-registration-opened-start-game-holder"}>
                        <button className={"competition-form-action-button"} onClick={startCompetition}>{t("waiting_room_new.start_game")}</button>
                    </div>
                </div>
            </div>
        </div>
    )
}

const CompetitionsParamsWrapper = ({formState, setFormState, saveAction}) => {
    const {t} = useTranslation();
    return (
        <div className={"after-registration-opened-competition-form-holder"}>
            <CompetitionParamsForm state={formState} onFormStateUpdated={(newFormState) => setFormState(newFormState)}/>
            <div className={"after-registration-opened-competition-form-holder-action-button-container"}>
                <button className={"competition-form-action-button"}
                        onClick={() => saveAction()}>{t("waiting_room_new.save")}</button>
            </div>
        </div>
    )
}

export default withRedirect(AfterRegistrationOpenedNewComponent);
