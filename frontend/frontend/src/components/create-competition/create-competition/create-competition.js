import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import CompetitionParamsForm from "../competition-params";
import "../competition-params/competition-params.css";
import ApiHelper from "../../../helpers/api-helper";
import {NotificationContainer} from "react-notifications";
import DefaultSubmitButton from "../../common/default-submit-button";
import withRedirect from "../../../helpers/redirect-helper";
import showNotification from "../../../helpers/notification-helper";

import {withTranslation} from "react-i18next";


class CreateCompetition extends React.Component {
    constructor(props) {
        super(props);

        this.formState = {};
    }

    componentDidMount() {
        if (this.props.location.state)
            this.initialState = this.props.location.state.initialState;
    }

    isUpdateMode() {
        return !!(this.props.match && this.props.match.params && this.props.match.params.pin);
    }

    onSaveAsDraftClick = () => {
        let obj = {...this.formState.toJSONObject(), state: "Draft"};

        if (!this.isUpdateMode()) {
            this.onCreateCompetition(obj, () => {
                this.props.history('/competitions/history')
            });
        } else {
            this.onUpdateDraftCompetition(obj, () => {
                this.props.history('/competitions/history');
            })
        }
    };

    onOpenRegistrationClick = () => {
        let obj = {...this.formState.toJSONObject(), state: "Registration"};

        if (!this.isUpdateMode()) {

            this.onCreateCompetition(obj, () => {
                console.log({pin: this.pin});
                this.props.history("/competitions/after_registration_opened/" + this.pin);
            })
        } else {
            this.onUpdateDraftCompetition(obj, () => {
                this.props.history("/competitions/after_registration_opened/" + this.props.match.params.pin);
            })
        }
    };

    onUpdateDraftCompetition = (obj, successCallback) => {
        const timeout = 2000;

        const {pin} = this.props.match.params;

        ApiHelper.updateCompetition(pin, obj)
            .catch(err => {
                showNotification(this).error(err, "Error", timeout);
            })
            .then(resp => {
                if (resp.status >= 300) {
                    return {success: false, response: resp.text()}
                }

                return {success: true, json: resp.json()};
            }).then(resp => {
                resp.json.then(jsonBody => {
                    if (resp.success) {
                        showNotification(this).success("Competition saved successfully", "Success!", timeout);
                        successCallback();
                    } else {
                        showNotification(this).error(jsonBody, "Error", timeout);
                    }
                })
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

                    showNotification(this).success("Competition created successfully", "Success!", timeout);
                    successCallback();
                })
            } else {
                console.log("Error");
                showNotification(this).error("Invalid competition params", "Error", timeout);
            }
        })
    };

    onFormStateUpdated = (formState) => {
        this.formState = formState;
    };

    render() {

        const {i18n} = this.props;

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <div style={{margin: "0 auto", textAlign: "center", fontSize: "36px"}}>
                        <span>{i18n.t('create_competition.create_game')}
                        </span>
                    </div>
                    <div className={"competition-form-holder"}>
                        <CompetitionParamsForm onFormStateUpdated={(formState) => this.onFormStateUpdated(formState)}
                                               initialState={this.props.location.state ? this.props.location.state.initialState : {}}/>
                        <div className={"form-group row"} style={{marginTop: "30px", marginLeft: "7.5%", marginRight: "7.5%"}}>
                            <div className={"mr-auto p-2"}>
                                <DefaultSubmitButton text={i18n.t('create_competition.save_draft')} style={{height: "100%", fontSize: "26px",
                                    paddingTop: "15.5px", paddingBottom: "15.5px"}} onClick={() => this.onSaveAsDraftClick()}/>
                            </div>
                            <div className={"p-2"}>
                                <DefaultSubmitButton text={i18n.t('create_competition.open_registration')} style={{height: "100%", fontSize: "26px",
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

export default withTranslation('translation')(withRedirect(CreateCompetition));