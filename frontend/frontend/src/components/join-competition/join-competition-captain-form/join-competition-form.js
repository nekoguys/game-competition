import React from "react";
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

import "./join-competition-form.css";


class JoinCompetitionForm extends React.Component {
    constructor(props) {
        super(props);

        this.formState = {
            teamName: "",
            password: ""
        };

        this.state = {
            isEnterGameButtonEnabled: false
        }
    }

    updateFormStateField(key, value) {
        this.formState[key] = value;

        this.setState({isEnterGameButtonEnabled: this.formState['teamName'].length >= 4 && this.formState['password'].length >= 4});
    }

    render() {
        const gameIdInputStyle = {
            padding: "20px",
            borderRadius: "25px",
        };

        const {onFormSubmit = (_f) => {}} = this.props;

        return <div style={this.props.style}>
            <div className={"game-id-container"}>
                <div>
                    <div style={gameIdInputStyle}>
                        <DefaultTextInput placeholder={"ID игры"} style={{
                            margin: "auto",
                            width: "25%",
                            borderRadius: "20px",
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            textAlign: "center"
                        }} onChange={(value) => {this.updateFormStateField('gameId', value)}}
                        />
                    </div>
                </div>
            </div>
            <div style={{padding: "40px 30px 40px 50px"}}>
                <div className={"game-info-form-container"}>
                    <div>
                        <DefaultTextInput placeholder={"Название команды"} style={{
                            margin: "auto",
                            width: "40%",
                            borderRadius: "20px",
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            textAlign: "center"
                        }} onChange={(value => {this.updateFormStateField('teamName', value)})}
                        />
                    </div>
                    <div>
                        <DefaultTextInput placeholder={"Пароль для входа"} style={{
                            margin: "auto",
                            width: "40%",
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            borderRadius: "20px",
                            marginTop: "32px",
                            textAlign: "center"
                        }} type={"password"} onChange={(value) => this.updateFormStateField('password', value)}/>
                    </div>
                    <div style={{paddingTop: "60px", paddingBottom: "10px"}}>
                        <DefaultSubmitButton text={"Войти в игру"} style={{
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            flexGrow: "0.2",
                        }} isDisabled={!this.state.isEnterGameButtonEnabled} onClick={() => onFormSubmit(this.formState)}/>
                    </div>
                </div>
            </div>
        </div>
    }
}

export default JoinCompetitionForm;
