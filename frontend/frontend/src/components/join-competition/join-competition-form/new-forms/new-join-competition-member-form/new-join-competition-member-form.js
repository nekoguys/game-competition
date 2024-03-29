import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {useNavigate, useParams} from "react-router";
import "../../../../../helpers/common.css";
import "./new-join-competition-member-form.css";
import NewTeamCollection from "./team-collection";
import {NavbarHeaderWithFetcher} from "../../../../app/app";
import {useWithAuthenticatedHook} from "../../../../../helpers/with-authenticated";

const NewJoinCompetitionMemberForm = ({fetchers, eventSources, showNotification}) => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [teams, setTeams] = useState([]);
    const [teamSearchString, setTeamSearchString] = useState("");

    useWithAuthenticatedHook();

    const matchParams = useParams();
    const {pin} = matchParams;

    useEffect(() => {
        let eventSource = eventSources.teams(pin);
        eventSource.subscribe((newTeam) => {
            console.log({newTeam, teams});
            setTeams(prevValue => {
                const teams = [...prevValue];
                const teamIndex = teams.map(el => el.idInGame).indexOf(newTeam.idInGame)
                if (teamIndex !== -1) {
                    teams[teamIndex] = newTeam;
                } else {
                    teams.push(newTeam);
                }
                return teams;
            })
        })

        return function cleanup() {
            eventSource.close();
        }
    }, [])

    const onSubmit = ({teamName, password}) => {
        fetchers.joinTeam(pin, {teamName, password}).then(_ => {
            showNotification().success("You joined team " + teamName + "!", "Success", 1500);
            setTimeout(() => {
                navigate("/competitions/waiting_room/" + pin);
            }, 1500);
        }).catch(err => {
            console.log(err);
            showNotification().error(`${err}`, "Error", 2500);
        })
    }
    const filteredTeams = teams.filter((team) => team.teamName.includes(teamSearchString));

    return (
        <div>
            <div>
                <NavbarHeaderWithFetcher/>
            </div>
            <div className={"below-navbar"} >
                <div >
                    <div className={"page-title create-team-form-title"}>
                        {t("join_competition.captain.enter_game_with_id") + ` №${pin}`}
                    </div>
                    <div className={"team-name-input-container"}>
                        <input placeholder={t('join_competition.member.find')} className={"team-name-input"} onChange={(ev) => setTeamSearchString(ev.target.value)}/>
                    </div>
                    <div className={"team-collection-root-container"}>
                        <div className={"new-team-collection-holder"}>
                            <NewTeamCollection teams={filteredTeams} onSubmit={onSubmit}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default NewJoinCompetitionMemberForm;
