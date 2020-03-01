import React from "react";
import "./room-teammates-collection.css";
import RoomTeammatesMembersCollection from "./room-teammates-members-collection";

class RoomTeammatesCollection extends React.Component {
    render() {
        const {style = {}} = this.props;
        const {ulstyle = {}} = this.props;
        const {items} = this.props;

        const elems = items.map(el => {
            return (<li key={el.idInGame}>
                <p>Team Name: {el.teamName}</p>
                <RoomTeammatesMembersCollection
                    items={el.teamMembers}
                    style={{marginRight: "20px", marginLeft: "20px"}}
                    ulstyle={{paddingTop: "20px", paddingBottom: "20px", marginBottom: "0"}}/>
            </li>)
        });

        return (
            <div className={"room-teammates-collection"} style={style}>
                <ul style={ulstyle}>
                    {elems}
                </ul>
            </div>
        )
    }
}


export default RoomTeammatesCollection;
