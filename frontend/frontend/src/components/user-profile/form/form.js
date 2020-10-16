import React from "react";
import DefaultTextInput from "../../common/default-text-input";

import "./form.css";
import {withTranslation} from "react-i18next";

class UserProfileForm extends React.Component {
    constructor(props) {
        super(props);

        const {initialFormState={}} = this.props;
    }

    updateFormStateField = (fieldName, value) => {
        const {onFormUpdated = (__) => {}} = this.props;

        onFormUpdated(fieldName, value);
    };


    render() {
        const textInputStyle = {
            margin: "0",
            width: "100%",
            textAlign: "left"
        };

        const { i18n } = this.props;

        return (
            <div>
                <div>
                    <div className={"row my-row"}>
                        <div className={"col-3 flex-center-vertically label-style"}>
                            {i18n.t('profile.last_name') + ":"}
                        </div>
                        <div className={"col-9"}>
                            <DefaultTextInput style={textInputStyle} onChange={(val) => {
                                this.updateFormStateField("surname", val);
                            }} defaultText={this.props.formState.surname}/>
                        </div>
                    </div>
                    <div className={"row my-row"}>
                        <div className={"col-3 flex-center-vertically label-style"} >
                            {i18n.t('profile.first_name') + ":"}
                        </div>
                        <div className={"col-9"}>
                            <DefaultTextInput style={textInputStyle} onChange={(val) => {
                                this.updateFormStateField("name", val);
                            }} defaultText={this.props.formState.name}/>
                        </div>
                    </div>
                    <div className={"row my-row"}>
                        <div className={"col-3 flex-center-vertically label-style"} >
                            {"Email:"}
                        </div>
                        <div className={"col-9"}>
                            <DefaultTextInput style={textInputStyle} readOnly={true} defaultText={this.props.formState.email}/>
                        </div>
                    </div>
                    <div className={"row my-row"}>
                        <div className={"col-3 flex-center-vertically label-style"} >
                            {i18n.t('profile.new_password') + ":"}
                        </div>
                        <div className={"col-9"}>
                            <DefaultTextInput style={textInputStyle} type={"password"} onChange={(val) => {
                                this.updateFormStateField("newPassword", val);
                            }}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(UserProfileForm);
