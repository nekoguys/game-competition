import React from "react";

import "./change-round-length-container.css";
import submitButtonImage from "../../../join-competition/join-competition-player-form/submitButton.png";
import {TextInputWithSubmitButton} from "../../../join-competition/join-competition-player-form/join-competition-player-form";

class ChangeRoundLengthContainer extends React.Component {
    render() {
        const buttonStyle = {
            backgroundColor: "Transparent",
            padding: "-5px",
            border: "none",
            overflow: "hidden",
            marginLeft: "-50px"
        };

        const inputStyle = {
            backgroundColor: "#CACACA",
            fontSize: "16px",
            textAlign: "center",
            border: "none",
            outline: "none"
        };

        return (
            <div className={"row"}>
                <div className={"col-4"} style={{textAlign: "right"}}>
                    Изменение длины раунда
                </div>
                <div className={"col-5"}>
                    <TextInputWithSubmitButton imagePath={submitButtonImage}
                                               containerStyle={{width: "100%", borderRadius: "10px"}}
                                               placeholder={""}
                                               inputStyle={inputStyle}
                                               buttonStyle={buttonStyle}
                                               onSubmit={this.props.changeRoundLengthCallback}
                                               clearOnSubmit={true}
                                               submitOnKey={'enter'}
                                               imgStyle={{width: "35px", height: "35px"}}
                    />
                    <div style={{paddingTop: "10px"}}>
                        <CurrentRoundLengthContainer currentRoundLength={this.props.currentRoundLength}/>
                    </div>
                </div>
            </div>
        )

    }
}

class CurrentRoundLengthContainer extends React.Component {
    render() {
        const {currentRoundLength} = this.props;
        const message = `Длина текущего раунда: ${currentRoundLength}`;
        return (
            <div className={"row current-round-length-container"}>
                {message}
            </div>
        )
    }
}

export default ChangeRoundLengthContainer;
