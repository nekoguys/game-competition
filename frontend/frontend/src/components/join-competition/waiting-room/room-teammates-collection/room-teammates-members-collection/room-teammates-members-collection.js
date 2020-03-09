import React from "react";
import "./room-teammates-members-collection.css";

class RoomTeammatesMembersCollection extends React.Component {

    render() {
        const {style = {}} = this.props;
        const {ulstyle = {}} = this.props;
        const {items} = this.props;

        const elems = items.map(el => {
            return (<li key={el}>
                {el}
            </li>)
        });

        return (
          <div className={"room-teammates-members-collection"} style={style}>
              <ul style={ulstyle}>
                  {elems}
              </ul>
          </div>
        );
    }
}

export default RoomTeammatesMembersCollection;