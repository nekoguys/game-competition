import React, {useEffect, useState} from "react";

import "./root.css";
import CompetitionResultsTable from "../../competition-process-teacher/results-table";
import {NavbarHeaderWithFetcher} from "../../../app/app";
import DescriptionHolder from "../../competition-process-student/description";
import {useTranslation} from "react-i18next";
import {useParams} from "react-router";
import {parseMessage} from "../../../../helpers/messages-event-source-helper";
import NewTeamCollection
    from "../../../join-competition/join-competition-form/new-forms/new-join-competition-member-form/team-collection";
import {SelfControlledDefaultToggle} from "../../competition-process-student/messages/messages";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";

const defaultState = {
    competitionName: "name",
    teams: [{teamMembers: ["a@emal", "b@emal"], teamName: "abbra", idInGame: 1},
        {teamMembers: ["c@emal", "d@emal"], teamName: "tutu", idInGame: 2}],
    produced: {1: {1: 10, 2: 20}, 2: {1: 30, 2: 40}},
    prices: {1: 6, 2: 5},
    results: {1: {1: -10, 2: -20}},
    messages: [{message: "message", dateStr: "11:52pm"}],
    teamsOrder: [2, 1],
    strategy: {}
}

const EndedCompetitionResultsRootNew = ({fetchers}) => {
    const [competitionState, setCompetitionState] = useState(defaultState);
    const {pin} = useParams();
    const {t} = useTranslation();

    const fetchResults = () => {
        fetchers.competitionResults(pin).then(resp => {
            const messages = resp.messages.map(message => parseMessage(message));
            const teamsSortedByGameId = resp.teams.sort((lhs, rhs) => lhs.idInGame - rhs.idInGame);
            setCompetitionState(prevState => {
                return {
                    ...prevState,
                    competitionName: resp.competitionName,
                    teams: teamsSortedByGameId,
                    teamsOrder: resp.teamsOrderInDecreasingByTotalPrice,
                    produced: resp.produced,
                    results: resp.income,
                    prices: resp.prices,
                    messages: messages,
                    instruction: resp.instruction,
                    strategy: resp.strategyHolders
                }
            })
        })
    }

    useEffect(() => fetchResults(), []);

    const teamsWithStrategy = competitionState.teams.map(team => {
        return {...team, strategy: {strategy: competitionState.strategy?.[team.idInGame]?.strategy, show: true}}
    })

    return (
        <div>
            <div>
                <NavbarHeaderWithFetcher/>
            </div>
            <div className={"below-navbar"}>
                <div className={"page-title competition-process-student-page-title"}>
                    {competitionState.competitionName + " №" + pin}
                    <br/>
                    {t('competition_results.ended')}
                </div>
                <div className={"competition-process-student-title-and-game-state-holder-spacer"}/>
                <div className={"game-state-holder  ended-results-table-visible-holder"}>
                    <div className={"ended-results-table-scroll-holder"}>
                        <CompetitionResultsTable
                            style={{width: "100%"}} teamsCount={competitionState.teams.length}
                            roundsCount={Object.keys(competitionState.prices).length}
                            answers={competitionState.produced}
                            results={competitionState.results}
                            prices={competitionState.prices}
                            teamsPermutation={competitionState.teamsOrder}
                            strategy={competitionState.strategy}
                        />
                    </div>


                    <div className={"ended-results-bottom-content"}>
                        <div className={"ended-results-table-messages-spacer"}/>
                        <SelfControlledDefaultToggle title={t("competition_results.teams")}>
                            <div className={"ended-results-teams-toggle-spacer"}/>
                            <NewTeamCollection teams={teamsWithStrategy} readOnly={true}/>
                        </SelfControlledDefaultToggle>
                        <div className={"ended-results-teams-description-spacer"}/>
                        <DescriptionHolder instruction={competitionState.instruction}/>
                        <div className={"ended-results-description-messages-spacer"}/>
                        <SelfControlledDefaultToggle title={t("competition_results.messages")}>
                            <div className={"ended-results-teams-toggle-spacer"}/>
                            <MessagesListContainer messages={competitionState.messages}/>
                        </SelfControlledDefaultToggle>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default EndedCompetitionResultsRootNew;
