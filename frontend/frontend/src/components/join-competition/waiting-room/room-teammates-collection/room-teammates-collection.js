import React from "react";
import "./room-teammates-collection.css";
import TeamCollection from "../../join-competition-player-form/team-collection";
import {withTranslation} from "react-i18next";

class RoomTeammatesCollection extends React.Component {
    render() {

        const {items, i18n} = this.props;

        return (
            <div style={{width: "80%", margin: "0 auto"}}>
                <TeamCollection i18n={i18n} showTeamMembers={this.props.showTeamMembers} items={items} isReadOnly={true}/>
            </div>
        );
    }
}

export default withTranslation('translation')(RoomTeammatesCollection);
