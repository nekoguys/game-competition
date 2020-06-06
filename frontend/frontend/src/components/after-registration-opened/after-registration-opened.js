import React from "react";
import "./after-registration-opened.css";
import TeamCollection from "../join-competition/join-competition-player-form/team-collection";
import buttonUpImage from "../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../join-competition/join-competition-player-form/team-collection/buttonDown.png";
import CompetitionParamsForm from "../create-competition/competition-params";
import ApiHelper from "../../helpers/api-helper";
import toCamelCase from "../../helpers/camel-case-helper";
import DefaultSubmitButton from "../common/default-submit-button";
import toSnakeCase from "../../helpers/snake-case-helper";
import getValueForJsonObject from "../../helpers/competition-params-helper";
import withRedirect from "../../helpers/redirect-helper";

import showNotification from "../../helpers/notification-helper";


class AfterRegistrationOpenedComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isExpanded: false,
            formState: {},
            items: []
        }
    }

    setupTeamEventConnection() {
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
                const elem = JSON.parse(event.data);
                const index = arr.findIndex(el => {return el.teamName === elem.teamName});
                if (index === -1) {
                    arr.push(elem);
                } else {
                    arr[index] = elem;
                }

                return {items: arr}
            });
        });
    }

    componentDidMount() {
        this.setupTeamEventConnection();

        const {pin} = this.props.match.params;

        ApiHelper.getCompetitionCloneInfo(pin).then((resp) => {
            console.log({resp});
            if (resp.status >= 300) {
                return {success: false, json: resp.json()}
            }
            return {success: true, json: resp.json()}
        }).then(resp => {
            resp.json.then(bodyJson => {
                if (resp.success) {
                    console.log({bodyJson});
                    console.log({camel: toCamelCase(bodyJson)});
                    this.setState({formState: toCamelCase(bodyJson)});
                }
            })
        })
    }

    componentWillUnmount() {
        if (this.eventSource !== undefined) {
            this.eventSource.close();
        }
    }

    startCompetition = (successCallback) => {
        const {pin} = this.props.match.params;

        ApiHelper.startCompetition(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()}
            }
            return {success: true, json: resp.json()}
        }).then(resp => {
            resp.json.then(bodyJson => {
                if (resp.success) {
                    successCallback(bodyJson.message);
                } else {
                    const mes = bodyJson.message || bodyJson;
                    showNotification(this).error(mes, "Error", 1200);
                }
            })
        })
    };

    updateCompetition = (additionalParams={}, successCallback=()=>{}) => {
        const {pin} = this.props.match.params;

        let jsonObj = {};

        Object.keys(this.state.formState).forEach(key => {
            jsonObj[toSnakeCase(key)] = getValueForJsonObject(key, this.state.formState[key]);
        });

        jsonObj = {...jsonObj, ...additionalParams};

        ApiHelper.updateCompetition(pin, jsonObj).then(resp => {
            console.log({resp});
            if (resp.status >= 300) {
                return {success: false, json: resp.text()}
            }
            return {success: true, json: resp.json()}
        }).then(resp => {
            resp.json.then(bodyJson => {
                if (resp.success) {
                    console.log({bodyJson});
                    console.log({camel: toCamelCase(bodyJson)});
                    successCallback(toCamelCase(bodyJson));
                } else {
                    const mes = bodyJson.message || bodyJson;
                    showNotification(this).error(mes, "Error", 1200);
                }
            })
        })
    };

    render() {
        const {pin} = this.props.match.params;
        console.log(pin);

        let res;

        const image = this.state.isExpanded ? buttonDownImage : buttonUpImage;

        if (this.state.isExpanded) {
            res = (
                <div>
                <div>
                    <CompetitionParamsForm initialState={this.state.formState} onFormStateUpdated={(state) => {
                    this.setState({formState: state})
                    }}/>
                </div>
                    <DefaultSubmitButton
                        text={"Сохранить"}
                        onClick={() => this.updateCompetition({}, (resp) => {
                            this.setState({formState: toCamelCase(resp)});
                            showNotification(this).success("Competition updated successfully", "Success", 900);
                        })}
                        style={{
                            width: "20%",
                            minWidth: "180px",
                            maxWidth: "250px",
                            paddingTop: "7px",
                            paddingBottom: "7px"
                             }}/>
                </div>
            )
        }

        return (
            <div>
                <div style={{fontSize: "31px"}}>
                    <div style={{textAlign: "center"}}>
                        {"Создание игры: " + pin}
                    </div>
                    <div style={{textAlign: "center"}}>
                        Регистрация открыта
                    </div>
                </div>
                <div className={"form-container"}>
                    <div style={{margin: "0 auto", width: "22%", minWidth:"250px", maxWidth: "320px"}}>
                    <div className={"show-settings"}
                         onClick={() => this.setState(prevState => {
                             return {isExpanded: !prevState.isExpanded};
                         })}>
                            <div style={{display: "inline"}}>
                        Показать настройки
                                </div>
                        <button style={{
                            border: "none",
                            backgroundColor: "Transparent",
                            marginLeft: "15px",
                            transform: "scale(0.35) translate(-20px, -5px)"
                        }}><img src={image} alt={"unwind"}/></button>
                    </div>
                    </div>
                    <div style={{paddingTop: "20px"}}>
                        {res}
                    </div>
                    <br/>
                    <div style={{paddingTop: "40px", width: "80%", margin: "0 auto"}}>
                        <TeamCollection items={this.state.items} isReadOnly={true}/>
                    </div>
                    <div style={{paddingTop: "40px", width: "25%", margin: "0 auto"}}>
                        <DefaultSubmitButton text={"Начать игру"} onClick={() => {

                            this.startCompetition(() => {
                                showNotification(this).success("Competition Started!", "Success", 1500);
                                setTimeout(() => {
                                    this.props.history.push("/competitions/process_teacher/" + pin)
                                }, 1500);
                            });
                        }} style={{padding: "10px 20px"}}/>
                    </div>
                </div>
            </div>
        );
    }
}

export default withRedirect(AfterRegistrationOpenedComponent);
