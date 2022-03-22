import React, {useRef, useState} from "react";
import "./new-team-collection.css";
import {useTranslation} from "react-i18next";

const NewTeamMembersElement = ({members}) => {
    const elems = members.map(el => {
        return (<li key={el}>
            {el}
        </li>)
    });
    return (
        <div className={"team-members-collection-holder"}>
            <ul>
                {elems}
            </ul>
        </div>
    )
}

const NewTeamCollectionElement = ({
                                      idInGame,
                                      teamName,
                                      isReadOnly = false,
                                      members,
                                      strategy = {show: false},
                                      onSubmit
                                  }) => {
    const {t} = useTranslation();
    const getName = () => {
        return t('competition_process.student.root.team') + (idInGame !== undefined ? idInGame.toString() + " - " : "") + teamName;
    }
    const inputRef = useRef(null);
    const [showingMembers, setShowingMembers] = useState(false);
    return (
        <div className={"new-team-element-root-holder"}>
            <div className={"new-team-element-holder"} onClick={() => setShowingMembers(!showingMembers)}>
                <div className={"element-content-container"}>
                    <div className={"team-name-text"}>{getName()}</div>
                    <div className={"spacer"}>
                    </div>
                    <input
                        ref={inputRef}
                        onClick={(ev) => ev.stopPropagation()}
                        onKeyDown={(ev) => {
                            if (ev.key === 'Enter') {
                                onSubmit({password: inputRef.current.value, teamName: teamName});
                            }
                        }}
                        className={"team-password-input " + (isReadOnly ? "team-password-input-hidden" : "")}
                    />
                    <div>
                        <img
                            src={"data:image/svg+xml,%3Csvg width='17' height='13' viewBox='0 0 17 13' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 12L8.69231 2L16 12' stroke='black' stroke-opacity='0.4' stroke-width='1.4359'/%3E%3C/svg%3E%0A"}
                            alt={"unwind"}
                            className={"team-dropdown-img" + (showingMembers ? " team-dropdown-img-expanded" : "")}
                        />
                    </div>
                </div>
            </div>
            <div className={"team-collection-unwind-content"}>
                <div className={showingMembers ? "team-members-container" : "team-members-container-hidden"}>
                    {
                        strategy.show ? <TeamMembersTitle/> : null
                    }
                    <div className={"ended-results-teams-title-content-spacer"}/>
                    <NewTeamMembersElement members={members}/>
                </div>
                <div className={"team-collection-strategy-members-spacer"}/>
                <div
                    className={(showingMembers && strategy.show) ? "team-collection-strategy-holder" : "team-collection-strategy-holder-hidden"}>
                    <TeamCollectionStrategyElement strategy={strategy.strategy}/>
                </div>
            </div>
        </div>
    )
}

const TeamMembersTitle = ({}) => {
    const {t} = useTranslation();
    return (
        <>
            <div className={"team-collection-strategy-title"}>{t("competition_results.team_members_title")}</div>
        </>
    )
}

const TeamCollectionStrategyElement = ({strategy}) => {
    const {t} = useTranslation();
    return (
        <>
            <div className={"team-collection-strategy-title"}>{t('competition_process.student.strategy') + ":"}</div>
            <div className={"ended-results-teams-title-content-spacer"}/>
            <span className={"team-collection-strategy-span"}>{strategy ? strategy : "Пустая стратегия"}</span>
        </>
    )
}

const NewTeamCollection = ({teams, onSubmit, readOnly = false}) => {
    console.log({teams});
    return (
        <div>
            {
                teams.map((team) => {
                    return <NewTeamCollectionElement
                        key={team.idInGame}
                        idInGame={team.idInGame}
                        teamName={team.teamName}
                        members={team.teamMembers}
                        strategy={team.strategy}
                        isReadOnly={readOnly}
                        onSubmit={onSubmit}
                    />
                })
            }
        </div>
    )
}

export default NewTeamCollection;
