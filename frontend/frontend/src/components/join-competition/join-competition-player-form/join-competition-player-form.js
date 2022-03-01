import React from "react";
import submitButtonImage from "./submitButton.png";
import ApiHelper from "../../../helpers/api-helper";

import TeamCollection from "./team-collection";
import DefaultTextInput from "../../common/default-text-input";
import {withRouter} from "../../../helpers/with-router";

import showNotification from "../../../helpers/notification-helper";

import "./join-competition-player-form.css";
import {withTranslation} from "react-i18next";
import {TextInputWithSubmitButton} from "./text-input-with-submit-button/text-input-with-submit-button";


class JoinCompetitionPlayerForm extends React.Component {

    constructor(props) {
        super(props);

        this.gameId = {};
        this.state = {
            currentPage: "gamePinPage",
            items: [],
            searchedTeamName: "",
            foundedTeams: [],
            showTeamMembers: true
        }
    }

    onSearchTeamNameChanged = (text) => {
        this.setState(prevState => {
            return {
                searchedTeamName: text,
                foundedTeams: prevState.items.filter(el => el.teamName.toLowerCase().includes(text))
            };
        })
    };

    onGameIdSubmitButton = (gameId) => {
        console.log(gameId);
        this.gameId = gameId;
        const timeout = 1500;
        ApiHelper.checkPin({pin: gameId}).then(resp => {
            if (resp.status >= 300) {
                return {success: false}
            }
            return {success: true, json: resp.json()};
        }).then(obj => {
            if (obj.success) {
                obj.json.then(val => {
                    console.log({val});
                    if (val.exists) {
                        showNotification(this).success("Competition found successfully", "Success", timeout);
                        this.fetchCompetitionInfo();
                        setTimeout(() => {
                            this.setState(prevState => {
                                return {currentPage: "enterTeamPage"};
                            })
                        }, timeout);
                    } else {
                        showNotification(this).error("No such competition or registration is closed", "Error", timeout);
                    }
                });
            } else {
                showNotification(this).error("Error happened", "Error", timeout);
            }
        })
    };

    fetchCompetitionInfo() {
        const pin = this.gameId;
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

    setupTeamEventConnections() {
        if (this.eventSource === undefined) {
            this.eventSource = ApiHelper.teamCreationEventSource(this.gameId);
            this.eventSource.addEventListener("error",
                (err) => {
                    console.log("EventSource failed: ", err)
                });
            this.eventSource.addEventListener("message", (event) => {
                console.log({data: event.data});
                this.setState((prevState) => {
                    let arr = prevState.items.slice(0);
                    const elem = JSON.parse(event.data);
                    if (elem.teamName === null) {
                        elem.teamName = "";
                    }
                    const index = arr.findIndex(el => {return el.teamName === elem.teamName});
                    if (index === -1) {
                        arr.push(elem);
                    } else {
                        arr[index] = elem;
                    }
                    
                    return {
                        items: arr,
                        foundedTeams: arr.filter(el => {
                            return el.teamName.toLowerCase().includes(prevState.searchedTeamName.toLowerCase())
                        })
                    }
                });
            });
        }
    }

    closeTeamEventConnections() {
        if (this.eventSource !== undefined)
            this.eventSource.close();
    }

    componentWillUnmount() {
        this.closeTeamEventConnections();
    }

    gamePinPage() {
        const {i18n} = this.props;

        return (
            <div style={{marginTop: "18px"}}>
                <div className={"join-player-form__input-container"}>
                    <TextInputWithSubmitButton imagePath={submitButtonImage} containerStyle={{margin: "0 auto"}}
                                               placeholder={i18n.t('join_competition.captain.game')} onSubmit={this.onGameIdSubmitButton}
                                               buttonStyle={{}} buttonClasses={" join-player-form__input-button"} inputStyle={{}}
                                               inputClasses={" join-player-form__input-input"}
                                               imgClasses={" join-player-form__input-img"}
                    />
                </div>
            </div>
        )
    }

    enterTeamPage() {
        const items = this.state.foundedTeams;
        return (
            <div style={{marginTop: "30px"}}>
                <div style={{marginTop: "10px", width: "50%", margin:"0 auto"}}>
                    <DefaultTextInput
                        placeholder={this.props.i18n.t('join_competition.member.find')}
                        style={{
                            width: "100%",
                            borderRadius: "20px",
                            paddingTop: "11px",
                            paddingBottom: "11px"
                          }}
                        onChange={this.onSearchTeamNameChanged}
                    />
                </div>
                <div style={{margin: "70px 15% 20px 15%",}}>
                <TeamCollection i18n={this.props.i18n} showTeamMembers={this.state.showTeamMembers} items={items} gamePin={this.gameId} onSubmit={this.onSubmit}
                />
                </div>
            </div>
        )
    }

    onSubmit = (teamName, password) => {
        console.log({teamName, password, props: this.gameId});
        const obj = {
            competitionPin: this.gameId,
            teamName: teamName,
            password: password
        };

        const timeout = 2200;

        ApiHelper.joinTeam(obj).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.json()};
            } else {
                return {success: true, json: resp.json()};
            }
        }).then(resp => {
            resp.json.then(obj => {
                console.log(obj);
                if (resp.success) {
                    const teamName = obj.currentTeamName;
                    window.localStorage.setItem("currentTeamName", teamName);
                    showNotification(this).success("You joined team " + teamName, "Success", timeout);
                    this.props.history("/competitions/waiting_room/" + this.gameId);

                } else {
                    showNotification(this).error(obj.message, "Error", timeout);
                }
            })
        })
    };

    render() {
        let res;
        if (this.state.currentPage === "gamePinPage") {
            res = this.gamePinPage();
            this.closeTeamEventConnections();
        } else {
            res = this.enterTeamPage();
            this.setupTeamEventConnections()
        }
        return (
            <div>
                {res}
            </div>
        )
    }
}

export default withTranslation('translation')(withRouter(JoinCompetitionPlayerForm));
