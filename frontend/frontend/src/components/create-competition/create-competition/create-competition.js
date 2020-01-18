import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import CompetitionParamsForm from "../competition-params";
import ApiHelper from "../../../helpers/api-helper";
import {NotificationManager, NotificationContainer} from "react-notifications";

class CreateCompetition extends React.Component {

    onSaveAsDraftClick = (formState) => {
        let obj = {...formState.toJSONObject(), state: "draft"};
        const timeout = 800;

        ApiHelper.createCompetition(obj).then(response => {
            console.log(response);
            return response.json();
        }).catch(err => {
            console.log(err);
            NotificationManager.error("Error happened", "Error", timeout);
        }).then(bodyJson => {
            console.log(bodyJson);
            NotificationManager.success("Competition created successfully", "Success!", timeout);
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
                    <CompetitionParamsForm onSaveAsDraftClick={this.onSaveAsDraftClick}/>
                    <NotificationContainer/>
                </div>
            </div>
        )
    }
}

export default CreateCompetition;