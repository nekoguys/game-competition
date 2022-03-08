import React, {useRef} from "react";
import {NavbarHeaderWithFetcher as NavbarHeader} from "../../app/app";
import CompetitionParamsForm from "../competition-params";
import "../competition-params/competition-params.css";
import withRedirect from "../../../helpers/redirect-helper";
import "./create-competition.css";

import {useTranslation} from "react-i18next";
import {useLocation, useNavigate, useParams} from "react-router";
import {makeStartingCompetitionForm, toCompetitionFormJsonObject} from "../../../helpers/competition-params-helper";


const CreateCompetition = ({isUpdateMode, fetchers, showNotification}) => {
    const {t} = useTranslation();
    const location = useLocation();
    const initialState = location?.state?.initialState || makeStartingCompetitionForm();
    const formState = useRef(initialState);
    const params = useParams();
    const navigate = useNavigate();

    const onSaveAsDraftClick = () => {
        const obj = {...toCompetitionFormJsonObject(formState.current), state: "Draft"};
        if (isUpdateMode) {
            onUpdateDraftCompetition(obj, () => {
                navigate("/competitions/history")
            });
        } else {
            onCreateCompetition(obj, () => {
                navigate("/competitions/history")
            });
        }
    }

    const onOpenRegistrationClick = () => {
        let obj = {...toCompetitionFormJsonObject(formState.current), state: "Registration"};

        if (isUpdateMode) {
            onUpdateDraftCompetition(obj, () => {
                navigate("/competitions/after_registration_opened/" + params.pin);
            })
        } else {
            onCreateCompetition(obj, (pin) => {
                navigate("/competitions/after_registration_opened/" + pin);
            })
        }
    };

    const onCreateCompetition = (obj, successCallback) => {
        const timeout = 2000;
        fetchers.createCompetition(obj).then(resp => {
            showNotification().success("Competition created successfully", "Success!", timeout);
            setTimeout(() => {
                successCallback(resp.pin);
            }, timeout);
        }).catch(err => {
            console.log("Error");
            showNotification().error(`Invalid competition params ${err}`, "Error", timeout);
        })
    }

    const onUpdateDraftCompetition = (obj, successCallback) => {
        const timeout = 2000;
        const {pin} = params;
        fetchers.updateCompetition(pin, obj).then(_ => {
            showNotification().success("Competition saved successfully", "Success!", timeout);
            successCallback();
        }).catch(err => {
            showNotification().error(err, "Error", timeout);
        })
    }

    const onFormStateUpdated = (newFormState) => {
        formState.current = newFormState;
    };

    return (
        <div>
            <div>
                <NavbarHeader/>
            </div>
            <div className={"below-navbar"}>

                <div className={"competition-form-holder"}>
                    <div className={"page-title create-game-title"}>
                        {t('create_competition.create_game')}
                    </div>
                    <CompetitionParamsForm onFormStateUpdated={(formState) => onFormStateUpdated(formState)}
                                           initialState={initialState}/>
                    <ActionsButtons t={t} actions={[onSaveAsDraftClick, onOpenRegistrationClick]}/>
                </div>
            </div>
        </div>
    )
}

const ActionsButtons = ({t, actions}) => {
    return (
        <div className={"competition-form-action-buttons-container"}>
            <button onClick={actions[0]} className={"competition-form-action-button"}>{t('create_competition.save_draft')}</button>
            <div className={"spacer"}/>
            <button onClick={actions[1]} className={"competition-form-action-button"}>{t('create_competition.open_registration')}</button>
        </div>
    )
}

export default withRedirect(CreateCompetition);