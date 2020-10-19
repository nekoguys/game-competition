import React from "react";
import "./team-members-collection.css";

class TeamMembersCollection extends React.Component {
    render() {
        const {style = {}} = this.props;
        const {ulstyle = {}} = this.props;
        const {items} = this.props;

        const elems = items.map(el => {
            return (<li key={el}>
                🥺👉👈
            </li>)
        });

        return (
            <div className={"rounded"} style={style}>
                <ul style={ulstyle}>
                    {elems}
                </ul>
            </div>
        )
    }
}

export default TeamMembersCollection;
