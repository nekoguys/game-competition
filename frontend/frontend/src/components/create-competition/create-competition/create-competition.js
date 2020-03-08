import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import CompetitionParamsForm from "../competition-params";
import "../competition-params/competition-params.css";
import ApiHelper from "../../../helpers/api-helper";
import {NotificationContainer, NotificationManager} from "react-notifications";
import DefaultSubmitButton from "../../common/default-submit-button";
import {withRouter} from "react-router-dom";


class CreateCompetition extends React.Component {
    constructor(props) {
        super(props);

        this.formState = {};
    }

    componentDidMount() {
        if (this.props.history.location.state)
            this.initialState = this.props.history.location.state.initialState;
    }

    onSaveAsDraftClick = () => {
        let obj = {...this.formState.toJSONObject(), state: "draft"};

        this.onCreateCompetition(obj, () => {});
    };

    onOpenRegistrationClick = () => {
        let obj = {...this.formState.toJSONObject(), state: "registration"};

        this.onCreateCompetition(obj, () => {
            console.log({pin: this.pin});
            this.props.history.push("/competitions/after_registration_opened/" + this.pin);
        })
    };

    onCreateCompetition = (obj, successCallback) => {
        const timeout = 800;

        ApiHelper.createCompetition(obj).then(response => {
            console.log(response);
            if (response.status >= 300) {
                return {success: false, response: response.body}
            }

            return {success: true, json: response.json()};
        }).catch(err => {
            console.log(err);
        }).then(result => {
            if (result.success) {
                return result.json.then(bodyJson => {
                    console.log(bodyJson);

                    if ("pin" in bodyJson) {
                        this.pin = bodyJson.pin;
                    }

                    NotificationManager.success("Competition created successfully", "Success!", timeout);
                    successCallback();
                })
            } else {
                console.log("Error");
                NotificationManager.error("Invalid competition params", "Error", timeout);
            }
        })
    };

    onFormStateUpdated = (formState) => {
        this.formState = formState;
    };

    render() {
        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <div style={{margin: "0 auto", textAlign: "center", fontSize: "36px"}}>
                        <span>Создание Игры
                        </span>
                    </div>
                    <div className={"competition-form-holder"}>
                        <CompetitionParamsForm onFormStateUpdated={(formState) => this.onFormStateUpdated(formState)}
                                               initialState={this.props.history.location.state ? this.props.history.location.state.initialState : {}}/>
                        <div className={"form-group row"} style={{marginTop: "30px", marginLeft: "7.5%", marginRight: "7.5%"}}>
                            <div className={"mr-auto p-2"}>
                                <DefaultSubmitButton text={"Сохранить черновик"} style={{height: "100%", fontSize: "26px",
                                    paddingTop: "15.5px", paddingBottom: "15.5px"}} onClick={() => this.onSaveAsDraftClick()}/>
                            </div>
                            <div className={"p-2"}>
                                <DefaultSubmitButton text={"Открыть регистрацию"} style={{height: "100%", fontSize: "26px",
                                    paddingTop: "15.5px", paddingBottom: "15.5px"}}
                                                     onClick={() => this.onOpenRegistrationClick()}/>
                            </div>
                        </div>
                    </div>
                </div>
                <NotificationContainer/>

            </div>
        )
    }
}

export default withRouter(CreateCompetition);