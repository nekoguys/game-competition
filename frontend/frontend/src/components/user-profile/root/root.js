import React from "react";

import "./root.css";
import {NavbarHeaderWithFetcher as NavbarHeader} from "../../app/app";
import UserProfileForm from "../form";
import ApiHelper from "../../../helpers/api-helper";
import DefaultSubmitButton from "../../common/default-submit-button";
import showNotification from "../../../helpers/notification-helper";
import withAuthenticated from "../../../helpers/with-authenticated";
import {withTranslation} from "react-i18next";
import {FunctionStorageWrapper} from "../../../helpers/storage-wrapper";


class UserProfileRoot extends React.Component {
    constructor(props) {
        super(props);

        this.needUpdateNavbarStorage = new FunctionStorageWrapper(
            () => this.state.needUpdateNavbar,
            (newValue) => this.setState({needUpdateNavbar: newValue})
        )

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

                    this.setState(() => {
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
        const {i18n} = this.props;
        console.log({state: this.state.formState});

        return (
            <div>
                <div>
                    <NavbarHeader needUpdateNavbarStorage={this.needUpdateNavbarStorage}/>
                </div>

                <div style={{paddingTop: "80px"}}>
                    <div style={{fontSize: "27px", textAlign: "center", paddingTop: "30px", paddingBottom: "30px"}}>
                        {i18n.t('profile.profile')}
                    </div>
                    <div className={"profile-form-holder"}>
                        <div>
                        <UserProfileForm onFormUpdated={this.onFormStateField} formState={this.state.formState}/>
                        </div>
                        <div style={{marginBottom: "-10px"}}>
                            <div style={{width: "15%", margin: "0 auto"}}>
                            <DefaultSubmitButton style={{width: "100%"}} text={i18n.t('profile.save')} onClick={this.onSave}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(withAuthenticated(UserProfileRoot));
