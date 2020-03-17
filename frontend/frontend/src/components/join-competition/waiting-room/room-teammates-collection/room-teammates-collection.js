import React from "react";
import "./room-teammates-collection.css";
import TeamCollection from "../../join-competition-player-form/team-collection";

class RoomTeammatesCollection extends React.Component {
    render() {

        const {items} = this.props;

        return (
            <div style={{width: "80%", margin: "0 auto"}}>
                <TeamCollection items={items} isReadOnly={true}/>
            </div>
        );
    }
}


export default RoomTeammatesCollection;
