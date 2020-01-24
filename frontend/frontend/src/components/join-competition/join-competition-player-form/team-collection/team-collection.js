import React from "react";
import "./team-collection.css";
import DefaultTextInput from "../../../common/default-text-input";
import buttonUpImage from "./buttonUp.png";

class TeamCollectionElement extends React.Component {
    constructor(props) {
        super(props);
        this.name = "Команда " + props.name;
    }

    render() {
        return (
            <div className={"item-container"}>
                <div className={"row"}>
                    <div className={"col-6"} style={{fontSize: "28px"}}>{this.name}</div>
                    <div className={"col-5"}>
                        <DefaultTextInput style={{margin: "0 10px 0 10px", float: "right"}}/>
                    </div>
                    <div style={{padding:"0px"}} className={"col-1"}>
                        <div style={{transform: "scale(0.3) translate(-30px, 0)"}}>
                            <button style={{
                                border: "none",
                                backgroundColor: "Transparent",
                                marginLeft: "15px"
                            }}><img src={buttonUpImage} alt={"unwind"}/></button>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

class TeamCollection extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        console.log(this.props.items);
        const items = this.props.items.map(item => {
            return (
                <div key={item.teamName}>
                    <TeamCollectionElement name={item.teamName}/>
                </div>
            )
        });

        return (
            <div className={"collection-container"}>
                {items}
            </div>
        )
    }
}

export default TeamCollection;
