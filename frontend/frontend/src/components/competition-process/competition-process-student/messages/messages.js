import React from "react";
import {MessagesListContainer} from "../../competition-process-teacher/messages/messages-container";
import {withTranslation} from "react-i18next";

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

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.state.isExpanded) {
            this.props.onReadMessagesCallback();
        }
    }

    render() {
        let res;
        let image = buttonUpImage;
        let badge;
        if (this.state.isExpanded) {
            res = (
                <div>
                    <MessagesListContainer messages={this.props.messages}/>
                </div>
            );
            image = buttonDownImage;
        }
        if (this.props.unreadMessages > 0) {
            badge = <span className={"badge count"}>{"+" + this.props.unreadMessages}</span>;
        }

        return (
            <div className={"d-flex justify-content-center"}>
                <div className={"col-6"} style={{minWidth: "250px"}}>
                    <div className={"show-messages"} style={{width: "40%", margin: "0 auto"}} onClick={this.expandOrCollapse}>
                        <div>
                        <div style={{display: "inline"}}>
                            {this.props.i18n.t("auth.login.enter")}
                        </div>
                        <button style={{
                            border: "none",
                            backgroundColor: "Transparent",
                            marginRight: "-10px",
                            transform: "scale(0.35) translate(-20px, -5px)"
                        }}><img src={image} alt={"unwind"}/></button>
                            {badge}
                        </div>
                    </div>
                    {res}
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(MessagesContainer);
