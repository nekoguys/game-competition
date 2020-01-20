import React from "react";
import "./join-competition-player-form.css";
import submitButtonImage from "./submitButton.png";

class TextInputWithSubmitButton extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            text: ""
        }
    }

    onTextChanged = (text) => {
        this.setState(prevState => {
            const {onChange = (_val) => {}} = this.props;
            onChange(text);
            return {
                text: text
            }
        })
    };

    render() {
        const {type="text", placeholder="", containerStyle={}, buttonStyle={},
            inputStyle={}, imagePath="", onSubmit=(_value) => {}} = this.props;
        return (
            <div style={{display: "flex", ...containerStyle}}>
                <input placeholder={placeholder} type={type} style={{flex: "1", ...inputStyle}}
                       onChange={event => this.onTextChanged(event.target.value)}/>
                <button style={buttonStyle} onClick={() => onSubmit(this.state.text)} type={"submit"}><img src={imagePath} alt={"submit competition id"}/></button>
            </div>
        )
    }
}

class JoinCompetitionPlayerForm extends React.Component {
    render() {
        const buttonStyle = {
            backgroundColor: "Transparent",
            padding: "-5px",
            border: "none",
            overflow: "hidden",
        };
        const inputStyle = {
            border: "none",
            outline: "none",
            backgroundColor: "Transparent",
            fontSize: "16px",
            textAlign: "center",
        };
        const innerContainerStyle = {
            width: "20%",
            margin: "0 auto",
            borderRadius: "20px",
            paddingTop: "5px",
            paddingBottom: "5px",
            backgroundColor: "white"
        };
        return (
            <div style={{marginTop: "40px"}}>
            <div style={innerContainerStyle}>
                <TextInputWithSubmitButton imagePath={submitButtonImage} containerStyle={{margin: "0 auto"}}
                                           placeholder={"ID игры"} onSubmit={(val) => console.log(val)}
                                           buttonStyle={buttonStyle} inputStyle={inputStyle}
                />
            </div>
            </div>
        )
    }
}

export default JoinCompetitionPlayerForm;
