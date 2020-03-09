import React from "react";
import "./waiting-room.css";
import ApiHelper from "../../../helpers/api-helper";

import {NotificationContainer} from "react-notifications";
import NavbarHeader from "../../competition-history/navbar-header";
import RoomTeammatesCollection from "./room-teammates-collection";

class WaitingRoom extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: []
        }
    }

    render() {
        this.setupTeamEventConnection();
        const items = this.state.items;
        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div>
                    <RoomTeammatesCollection items={items}
                                             style={{paddingTop: "100px", fontSize: "20px"}}
                                             ulstyle={{listStyle: "none", MarginTop: "-10px"}}
                    />
                </div>
                <NotificationContainer/>
            </div>
        )
    }

    setupTeamEventConnection() {
        if (this.eventSource === undefined) {

            const {pin} = this.props.match.params;

            this.eventSource = ApiHelper.teamCreationEventSource(pin);
            this.eventSource.addEventListener("error",
                (err) => {
                    console.log("EventSource failed: ", err)
                });
            this.eventSource.addEventListener("message", (event) => {
                console.log({data: event.data});
                this.setState((prevState) => {

                    let arr = prevState.items.slice(0);

                    console.log(prevState.items);
                    const elem = JSON.parse(event.data);
                    let index = arr.findIndex(el => {
                        return el.teamName === elem.teamName
                    });
                    if (index === -1) {
                        arr.push(elem);
                    } else {
                        arr[index] = elem;
                    }

                    return {items: arr}
                });
            });

            // TODO: can we do smth like this?
            /*
            this.eventSource.addEventListener("gamestart", (event) => {
               console.log("GAME HAS STARTED!");
               // TODO: redirect to game
            });

             */
        }
    }
}

export default WaitingRoom;
