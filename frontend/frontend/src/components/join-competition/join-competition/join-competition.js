import React from "react";
import JoinCompetitionForm from "../join-competition-captain-form";
import DefaultCheckboxButtonGroup from "../../common/default-checkbox-button-group";
import NavbarHeader from "../../competition-history/navbar-header/navbar-header";
import toSnakeCase from "../../../helpers/snake-case-helper";
import JoinCompetitionPlayerForm from "../join-competition-player-form";
import ApiHelper from "../../../helpers/api-helper";
import {withRouter} from "react-router-dom";

import showNotification from "../../../helpers/notification-helper";
import withAuthenticated from "../../../helpers/with-authenticated";


class JoinCompetition extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            currentPage: "captain"
        }
    }

    onCreateTeamPageClick = () => {
        console.log("createTeamPageClick");
        this.setState(() => {
            return {currentPage: "captain"};
        })
    };

    onJoinTeamPageClick = () => {
        console.log("onJoinTeamPageClick");
        this.setState(() => {
            return {currentPage: "player"};
        })
    };

    onCreateTeamClick = (formState) => {
        let obj = {captain_email: window.localStorage.getItem('user_email')};
        Object.keys(formState).forEach(key => {
            obj[toSnakeCase(key)] = formState[key];
        });
        console.log(obj);
        const timeout = 2000;
        ApiHelper.createTeam(obj).then(resp => {
            console.log(resp);
            resp.clone().text().then(res => console.log(res));
            if (resp.status >= 300) {
                return {success: false, json: resp.clone().json()};
            }
            return {success: true, json: resp.clone().json()};
        }).then(obj => {
            obj.json.then(respMessage => {
                console.log(respMessage);
                if (obj.success) {
                    showNotification(this).success("Team created successfully", "Success", timeout);
                    this.props.history.push("/competitions/waiting_room/" + formState.gameId);
                } else {
                    showNotification(this).error(respMessage.message, "Error", timeout);
                }
            })
        })
    };

    render() {
        let res;
        if (this.state.currentPage === "captain") {
            res = <JoinCompetitionForm showNotification={this.props.showNotification} onFormSubmit={this.onCreateTeamClick}/>
        } else {
            res = <JoinCompetitionPlayerForm showNotification={this.props.showNotification}/>
        }

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
            <div style={{paddingTop: "90px"}}>
                <div className={"d-flex"}>
                    <div style={{margin: "0 auto", width: "26%"}}>
                        <DefaultCheckboxButtonGroup
                            choices={["Капитан", "Участник"]}
                            buttonStyle={{width: "51%", fontSize: "1.3rem",
                                textOverflow: "ellipsis", overflow: "hidden"}}
                            style={{width: "100%"}}
                            onChoiceChanged={[
                                this.onCreateTeamPageClick,
                                this.onJoinTeamPageClick
                            ]}/>
                    </div>
                </div>
                {res}
            </div>
            </div>
        )
    }
}

export default withAuthenticated(JoinCompetition);
