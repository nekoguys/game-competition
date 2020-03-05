import React from "react";
import buttonUpImage from "../../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../../join-competition/join-competition-player-form/team-collection/buttonDown.png";


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
            <div style={{width: "100%"}}>
                <div className={"show-description"} style={{width: "20%", margin: "0 auto"}} onClick={(e) =>{
                    this.setState(prevState=> {return {isExpanded: !prevState.isExpanded};});
                }}>
                    <div style={{display: "inline"}}>
                        Описание
                    </div>
                    <button style={{
                        border: "none",
                        backgroundColor: "Transparent",
                        marginRight: "-10px",
                        transform: "scale(0.35) translate(-20px, -5px)"
                    }}><img src={image} alt={"unwind"}/></button>
                </div>
                {res}
            </div>
        )
    }
}

export default DescriptionHolder;
