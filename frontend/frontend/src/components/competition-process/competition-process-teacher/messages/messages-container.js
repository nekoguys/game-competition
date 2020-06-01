import React from "react";
import {TextInputWithSubmitButton} from "../../../join-competition/join-competition-player-form/join-competition-player-form.js";
import submitButtonImage from "../../../join-competition/join-competition-player-form/submitButton.png";
import "./messages-container.css";

class MessagesContainer extends React.Component {
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
                <div className={"col-4"} style={{textAlign: "right", paddingTop: "5px"}}>
                    {"Сообщение студентам"}
                </div>
                <div className={"col-5"}>
                    <TextInputWithSubmitButton imagePath={submitButtonImage}
                                               containerStyle={{width: "100%", borderRadius: "10px"}}
                                               placeholder={""}
                                               inputStyle={inputStyle}
                                               buttonStyle={buttonStyle}
                                               onSubmit={this.props.sendMessageCallBack}
                                               clearOnSubmit={true}
                                               submitOnKey={'enter'}
                                               imgStyle={{width: "35px", height: "35px"}}
                    />
                    <div style={{paddingLeft: "40px"}}>
                        <MessagesListContainer messages={this.props.messages}/>
                    </div>
                </div>
            </div>
        )
    }
}

export class MessagesListContainer extends React.Component {
    render() {
        const {messages = []} = this.props;
        return (
            <div style={{paddingRight: "15px"}}>
                    {messages.map(el => {
                        return <div key={el.dateStr+el.message} style={{marginTop: "7px"}}><SingleMessageContainer message={el}/></div>
                    })}
            </div>
        )
    }
}

class SingleMessageContainer extends React.Component {
    render() {
        const {message} = this.props;
        const messageStyle = {
            backgroundColor: "#CACACA",
            borderRadius: "10px"
        };
        return (
            <div style={messageStyle}>
                <div style={{fontSize: "19px", display: "flex", flexDirection: "column", justifyContent: "space-between", paddingTop: "2px"}}>
                    <div style={{paddingTop: "2px", paddingLeft: "10px", marginBottom: "-10px", wordBreak: "break-all"}}>
                        {message.message}
                    </div>
                    <div style={{ textAlign: "right", fontSize: "11px", paddingRight: "20px", paddingTop: "5px"}}>
                        {message.dateStr}
                    </div>
                </div>
            </div>
        )
    }
}

export default MessagesContainer;
