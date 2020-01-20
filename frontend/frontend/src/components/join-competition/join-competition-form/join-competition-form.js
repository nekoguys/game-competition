import React from "react";
import "./join-competition-form.css";
import DefaultTextInput from "../../common/default-text-input";
import DefaultSubmitButton from "../../common/default-submit-button";

class JoinCompetitionForm extends React.Component {
    render() {
        const gameIdInputStyle = {
            padding: "20px",
            borderRadius: "25px",
        };
        return <div style={this.props.style}>
            <div className={"game-id-container"}>
                <div>
                    <div style={gameIdInputStyle}>
                        <DefaultTextInput placeholder={"ID игры"} style={{
                            margin: "auto",
                            width: "25%",
                            borderRadius: "20px",
                            paddingTop: "10px",
                            paddingBottom: "10px"
                        }}/>
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
                            paddingBottom: "10px"
                        }}/>
                    </div>
                    <div>
                        <DefaultTextInput placeholder={"Пароль для входа"} style={{
                            margin: "auto",
                            width: "40%",
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            borderRadius: "20px",
                            marginTop: "32px",
                        }}/>
                    </div>
                    <div style={{paddingTop: "60px", paddingBottom: "10px"}}>
                        <DefaultSubmitButton text={"Войти в игру"} style={{
                            paddingTop: "10px",
                            paddingBottom: "10px",
                            flexGrow: "0.2"
                        }}/>
                    </div>
                </div>
            </div>
        </div>
    }
}

export default JoinCompetitionForm;
