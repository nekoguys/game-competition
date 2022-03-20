import React, {useEffect, useState} from "react";
import "./waiting-room.css";

import {NavbarHeaderWithFetcher as NavbarHeader} from "../../app/app";
import withAuthenticated from "../../../helpers/with-authenticated";

import {useNavigate, useParams} from "react-router";
import {useTranslation} from "react-i18next";


const NewStudentsWaitingRoom = ({eventSources, fetchers, showNotification}) => {
    const {t} = useTranslation();
    const [teamMembers, setTeamMembers] = useState([]);
    const [teamPassword, setTeamPassword] = useState();
    const [teamName, setTeamName] = useState("");
    const {pin} = useParams();
    const navigate = useNavigate()

    useEffect(() => {
        const eventSource = eventSources.teamMembers(pin);
        eventSource.subscribe((newTeamMember) => {
            setTeamMembers(prevValues => [...prevValues, newTeamMember]);
        })

        return function cleanup() {
            eventSource.close();
        }
    }, [])

    useEffect(() => {
        const eventSource = eventSources.competitionRoundEvents(pin);
        const timeout = 1500
        eventSource.subscribe((_) => {
            showNotification().warning("Game started!", "Attention", timeout);

            setTimeout(() => {
                navigate("/competitions/process_captain/" + pin);
            }, timeout + 100);
        })

        return function cleanup() {
            eventSource.close();
        }
    }, [])

    useEffect(() => {
        fetchers.teamNameAndPassword(pin).then(resp => {
            if (!Object.is(resp.password, null)) {
                setTeamPassword(resp.password);
            }
            setTeamName(resp.teamName)
        })
    }, [])

    const passwordAndSpacer = teamPassword !== undefined
        ? (<div>
            <div>
                <TeamPasswordContainer password={teamPassword}/>
            </div>
            <div className={"waiting-room-password-field-members-spacer"}/>
        </div>)
        : null;

    return (
        <div>
            <div>
                <NavbarHeader/>
            </div>
            <div className={"below-navbar"}>
                <div className={"waiting-room-page-title page-title"}>
                    {t("waiting_room_new.students_page_title") + pin}
                </div>
                <div className={"waiting-room-title-content-spacer"}/>
                <div className={"waiting-room-main-content"}>
                    <div className={"waiting-room-main-content-team-name"}>
                        {teamName}
                    </div>
                    {passwordAndSpacer}
                    <div>
                        <div className={"waiting-room-main-content-members-title"}>
                            {t("waiting_room_new.students_team_members")}
                        </div>
                        <div className={"waiting-room-members-container-wrapper"}>
                            <MembersContainer members={teamMembers}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

const MembersContainer = ({members}) => {
    const membersElements = members.map((el, ind) => {
        return (
            <li key={ind}>{el.name}</li>
        )
    })
    return (
        <div>
            <ul className={"waiting-room-members-container"}>
                {membersElements}
            </ul>
        </div>
    )
}

const TeamPasswordContainer = ({password = ""}) => {
    const [shown, setShow] = useState(false)

    const togglePasswordVisibility = (event) => {
        setShow(!shown);
        event.stopPropagation()
    }

    return (
        <div className={"waiting-room-team-password-container"}>
            {shown
                ? <div className={"waiting-room-team-password-shown-password"}>{password}</div>
                : <div className={"waiting-room-team-password-show-text"}>{"Показать пароль для входа"}</div>
            }
            <div className={"waiting-room-show-button-container"} onClick={togglePasswordVisibility}>
                <img alt={"show password"}
                     className={shown ? "waiting-room-show-button-shown" : "waiting-room-show-button"}/>
            </div>
        </div>
    )
}

export default withAuthenticated(NewStudentsWaitingRoom);
