import React from "react";
import buttonUpImage from "../../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../../join-competition/join-competition-player-form/team-collection/buttonDown.png";
import {withTranslation} from "react-i18next";

import "./description.css";

class DescriptionHolder extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isExpanded: false
        }
    }


    render() {
        let image = buttonUpImage;
        let res;
        if (this.state.isExpanded) {
            res = (
                <div style={{paddingTop: "10px"}}>
                <div className={"instruction-holder"}>
                    {this.props.instruction}
                </div>
                </div>
            );
            image = buttonDownImage;
        }

        return (
            <div style={{width: "100%", paddingLeft: "30px", paddingRight: "30px"}}>
                <div className={"show-description row justify-content-center"} style={{width: "20%", margin: "0 auto", position: "relative"}} onClick={(e) =>{
                    this.setState(prevState=> {return {isExpanded: !prevState.isExpanded};});
                }}>
                    <div className={"col-7"} style={{padding: 0, margin: "5px 0"}}>
                        {this.props.i18n.t("competition_process.student.description.description")}
                    </div>
                    <div className={"col-2"} style={{padding: 0, height: 0}}>
                    <button style={{
                        border: "none",
                        backgroundColor: "Transparent",
                        marginRight: "-10px",
                        transform: "scale(0.35) translate(-20px, -30%)"
                    }}><img src={image} alt={"unwind"}/></button>
                    </div>
                </div>
                {res}
            </div>
        )
    }
}

export default withTranslation('translation')(DescriptionHolder);
