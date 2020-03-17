import React from "react";

import "./root.css";
import NavbarHeader from "../../competition-history/navbar-header/navbar-header";
import UserProfileForm from "../form";
import ApiHelper from "../../../helpers/api-helper";
import DefaultSubmitButton from "../../common/default-submit-button";
import showNotification from "../../../helpers/notification-helper";
import withAuthenticated from "../../../helpers/with-authenticated";


class UserProfileRoot extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            formState: {
                name: "",
                surname: "",
                needUpdateNavbar: false
            }
        }
    }

    componentDidMount() {
        this.getProfileInfo();
    }

    getProfileInfo() {
        ApiHelper.getProfile().then(resp => {
            console.log({resp});
            let json;
            if (resp.status >= 300) {
                json = resp.text();
            } else {
                json = resp.json();
            }

            json.then(jsonBody => {
                console.log({jsonBody});
                this.setState({
                    formState: {
                        name: jsonBody.name,
                        surname: jsonBody.surname,
                        email: jsonBody.email,
                    }
                })
            })
        })
    }

    onFormStateField = (fieldName, value) => {
        this.setState(prevState => {
            const formState = {...prevState.formState};
            formState[fieldName] = value;
            return {formState};
        })
    };

    onSave = () => {
        ApiHelper.updateProfile(this.state.formState).catch(err => {
            showNotification(this).error("Something went wrong, try to login one more time", "Error", 3000);
        }).then(resp => {
            if (resp.status >= 300) {
                resp.text().then(txt => showNotification(this).error(txt, "Error", 2000));
            } else {
                resp.json().then(jsonBody => {
                    showNotification(this).success(jsonBody.message, "Success", 1200);

                    this.getProfileInfo();

                    this.setState(prevState => {
                        return {needUpdateNavbar: true};
                    })
                })
            }
        })
    };

    onNoNeedUpdateNavbar = () => {
        this.setState({needUpdateNavbar: false});
    };

    render() {
        console.log({state: this.state.formState});
        return (
            <div>
                <div>
                    <NavbarHeader onNoNeedUpdateNavbar={this.onNoNeedUpdateNavbar} needUpdate={this.state.needUpdateNavbar}/>
                </div>

                <div style={{paddingTop: "80px"}}>
                    <div style={{fontSize: "27px", textAlign: "center", paddingTop: "30px", paddingBottom: "30px"}}>
                        {"Профиль"}
                    </div>
                    <div className={"profile-form-holder"}>
                        <div>
                        <UserProfileForm onFormUpdated={this.onFormStateField} formState={this.state.formState}/>
                        </div>
                        <div style={{marginBottom: "-10px"}}>
                            <div style={{width: "15%", margin: "0 auto"}}>
                            <DefaultSubmitButton style={{width: "100%"}} text={"Сохранить"} onClick={this.onSave}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withAuthenticated(UserProfileRoot);
