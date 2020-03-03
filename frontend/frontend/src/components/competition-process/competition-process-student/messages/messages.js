import React from "react";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";


import "./messages.css";
import buttonUpImage from "../../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../../join-competition/join-competition-player-form/team-collection/buttonDown.png";

class MessagesContainer extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isExpanded: false
        }
    }

    expandOrCollapse = () => {
        this.setState((prevState) => {
            return {isExpanded: !prevState.isExpanded};
        })
    };

    render() {
        let res;
        let image = buttonUpImage;
        if (this.state.isExpanded) {
            res = (
                <div>
                    <MessagesListContainer messages={this.props.messages}/>
                </div>
            );
            image = buttonDownImage;
        }

        return (
            <div className={"d-flex justify-content-center"}>
                <div className={"col-6"}>
                    <div className={"show-messages"} style={{width: "40%", margin: "0 auto"}} onClick={this.expandOrCollapse}>
                        <div>
                        <div style={{display: "inline"}}>
                            Сообщения
                        </div>
                        <button style={{
                            border: "none",
                            backgroundColor: "Transparent",
                            marginRight: "-10px",
                            transform: "scale(0.35) translate(-20px, -5px)"
                        }}><img src={image} alt={"unwind"}/></button>
                        <span className={"badge count"}>+3</span>
                        </div>
                    </div>
                    {res}
                </div>
            </div>
        )
    }
}

export default MessagesContainer;
