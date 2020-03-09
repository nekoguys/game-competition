import React from "react";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";

class ReadonlyMessagesContainer extends React.Component {
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
                <div className={"col-3"} style={{textAlign: "right", paddingTop: "10px"}}>
                    {"Сообщения:"}
                </div>
                <div className={"col-6"}>
                    <div>
                        <MessagesListContainer messages={this.props.messages}/>
                    </div>
                </div>
            </div>
        )
    }
}

export default ReadonlyMessagesContainer;