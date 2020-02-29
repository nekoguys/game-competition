import React from "react";
import "./room-teammates-collection.css";

class RoomTeammatesCollection extends React.Component {
    render() {
        const {style = {}} = this.props;
        const {ulstyle = {}} = this.props;
        const {items} = this.props;

        if (items === undefined)
            return;

        const elems = items.map(el => {
            return (<li key={el}>
                {el}
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
