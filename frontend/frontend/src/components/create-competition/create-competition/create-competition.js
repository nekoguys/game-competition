import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import CompetitionParamsForm from "../competition-params";
import ApiHelper from "../../../helpers/api-helper";
import {NotificationManager, NotificationContainer} from "react-notifications";

class CreateCompetition extends React.Component {

    onSaveAsDraftClick = (formState) => {
        let obj = {...formState.toJSONObject(), state: "draft"};

        this.onCreateCompetition(obj, () => {});
    };

    onOpenRegistrationClick = (formState) => {
        let obj = {...formState.toJSONObject(), state: "registration"};

        this.onCreateCompetition(obj, () => {})
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
                    NotificationManager.success("Competition created successfully", "Success!", timeout);
                    successCallback();
                })
            } else {
                console.log("Error");
                NotificationManager.error("Invalid competition params", "Error", timeout);
            }
        })
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
                    <CompetitionParamsForm onSaveAsDraftClick={this.onSaveAsDraftClick} onOpenRegistrationClick={this.onOpenRegistrationClick}/>
                    <NotificationContainer/>
                </div>
            </div>
        )
    }
}

export default CreateCompetition;