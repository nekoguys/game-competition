import React, {useRef, useState} from "react";
import buttonUpImage from "../../../../join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../../../join-competition-player-form/team-collection/buttonDown.png";
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

const NewTeamCollectionElement = ({idInGame, teamName, isReadOnly= false, members, onSubmit}) => {
    const {t} = useTranslation();
    const getName = () => {
        return t('competition_process.student.root.team') + (idInGame !== undefined ? idInGame.toString() + " - " : "") + teamName;
    }
    const inputRef = useRef(null);
    const [showingMembers, setShowingMembers] = useState(false);
    const image = showingMembers ? buttonUpImage : buttonDownImage;
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
                        <img src={image} alt={"unwind"} className={"team-dropdown-img"}/>
                    </div>
                </div>
            </div>
            <div className={showingMembers ? "team-members-container" : "team-members-container-hidden"}>
                <NewTeamMembersElement members={members}/>
            </div>
        </div>
    )
}

const NewTeamCollection = ({teams, onSubmit, readOnly=false}) => {
    return (
        <div>
        {
            teams.map((team) => {
                return <NewTeamCollectionElement
                    key={team.idInGame}
                    idInGame={team.idInGame}
                    teamName={team.teamName}
                    members={team.teamMembers}
                    isReadOnly={readOnly}
                    onSubmit={onSubmit}
                />
            })
        }
        </div>
    )
}

export default NewTeamCollection;
