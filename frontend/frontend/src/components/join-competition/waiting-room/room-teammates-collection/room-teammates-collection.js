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
                <p>Команда "{el.teamName}":</p>
                <RoomTeammatesMembersCollection
                    items={el.teamMembers}
                    style={{marginRight: "20px", marginLeft: "20px"}}
                    ulstyle={{paddingBottom: "20px", paddingTop: "0px"}}/>
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
