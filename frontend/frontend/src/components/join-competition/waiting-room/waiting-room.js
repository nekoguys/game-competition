import React from "react";
import "./waiting-room.css";
import ApiHelper from "../../../helpers/api-helper";

import {NotificationContainer, NotificationManager} from "react-notifications";
import NavbarHeader from "../../competition-history/navbar-header";
import RoomTeammatesCollection from "./room-teammates-collection";
import {withRouter} from "react-router-dom";
import withAuthenticated from "../../../helpers/with-authenticated";


class WaitingRoom extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: []
        }
    }

    componentDidMount() {
        this.setupTeamEventConnection();
        this.setupGameStartListener();
    }

    componentWillUnmount() {
        if (this.eventSource !== undefined) {
            this.eventSource.close();
        }
        this.closeGameStartListener();
    }

    closeGameStartListener = () => {
        if (this.gameStartListener !== undefined) {
            this.gameStartListener.close();
        }
    };

    render() {
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

    setupGameStartListener() {
        const {pin} = this.props.match.params;

        this.gameStartListener = ApiHelper.competitionRoundEventsStream(pin);

        this.gameStartListener.addEventListener("message", (message) => {
            this.closeGameStartListener();
            console.log("game started");
            const timeout = 1500;

            NotificationManager.warning("Game started!", "Attention", timeout);

            setTimeout(() => {
                this.props.history.push("/competitions/process_captain/" + pin);
            }, timeout + 100);
        });
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
        }
    }
}

export default withAuthenticated(WaitingRoom);
