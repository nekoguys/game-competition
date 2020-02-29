import React from "react";
import "./waiting-room.css";
import ApiHelper from "../../../helpers/api-helper";

import {NotificationContainer, NotificationManager} from "react-notifications";
import NavbarHeader from "../../competition-history/navbar-header";
import TeamCollection from "../join-competition-player-form/team-collection";
import RoomTeammatesCollection from "./team-members-collection";

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
                <p>a</p>
                <p>a</p>
                <p>a</p>
                <p>a</p>
                <div>
                    <RoomTeammatesCollection items={items}/>
                </div>
                <NotificationContainer/>
            </div>
        )
    }

    setupTeamEventConnection() {
        if (this.eventSource === undefined)
        {

            const {pin} = this.props.match.params;

            this.eventSource = ApiHelper.teamCreationEventSource(pin);
            this.eventSource.addEventListener("error",
                (err) => {
                    console.log("EventSource failed: ", err)
                });
            this.eventSource.addEventListener("message", (event) => {
                console.log({data: event.data});
                this.setState((prevState) => {
                    if (prevState === undefined)
                        return "oops";
                    if (prevState.items === undefined)
                        return "oops";
                    let arr = prevState.items.slice(0);
                    if (arr === undefined)
                        return ("oops");
                    const elem = JSON.parse(event.data);
                    let index = arr.findIndex(el => {
                        return el.teamName === elem.teamName
                    });
                    if (index === -1) {
                        arr.push(elem);
                    } else {
                        arr[index] = elem;
                    }

                    let retArr;
                    arr.forEach(x => {
                        if (x.teamMembers !== undefined) {
                            if (x.teamMembers
                                .findIndex(el => {
                                    return el === window.localStorage.getItem("user_email")
                                }) !== -1) {
                                retArr = x.teamMembers;
                            }
                        }
                    });

                    if (retArr === undefined)
                        return {items: arr[0].teamMembers};
                    else
                        return {items: retArr}
                });
            });
        }
    }

}

export default WaitingRoom;
