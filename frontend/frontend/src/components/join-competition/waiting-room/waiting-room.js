import React from "react";
import "./waiting-room.css";
import ApiHelper from "../../../helpers/api-helper";

import NavbarHeader from "../../competition-history/navbar-header";
import RoomTeammatesCollection from "./room-teammates-collection";
import withAuthenticated from "../../../helpers/with-authenticated";

import showNotification from "../../../helpers/notification-helper";


class WaitingRoom extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: [],
            showTeamMembers: true
        }
    }

    componentDidMount() {
        this.setupTeamEventConnection();
        this.setupGameStartListener();
        this.fetchCompetitionInfo();
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
                    <div style={{paddingTop: "120px"}}>
                        <div style={{fontSize: "28px", margin: "0 auto", textAlign: "center"}}>
                            {"Комната ожидания"}
                        </div>
                    </div>
                    <div style={{paddingTop: "40px"}}>
                    <RoomTeammatesCollection items={items}
                                             style={{paddingTop: "100px", fontSize: "20px"}}
                                             ulstyle={{listStyle: "none", MarginTop: "-10px"}}
                                             showTeamMembers={this.state.showTeamMembers}
                    />
                    </div>
                </div>
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

            showNotification(this).warning("Game started!", "Attention", timeout);

            setTimeout(() => {
                this.props.history("/competitions/process_captain/" + pin);
            }, timeout + 100);
        });
    }

    fetchCompetitionInfo() {
        const {pin} = this.props.match.params;
        ApiHelper.competitionInfoForTeams(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()}
            } else {
                return {success: true, json: resp.json()}
            }
        }).then(resp => {
            resp.json.then(jsonBody => {
                if (resp.success) {
                    console.log("ShowTeamMembers", jsonBody.showTeamMembers, jsonBody);
                    this.setState(prevState => {
                        return {
                            showTeamMembers: jsonBody.showTeamMembers ?? true
                        }
                    })
                }
            })
        })
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
