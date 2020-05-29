import React from "react";
import "./team-collection.css";
import DefaultTextInput from "../../../common/default-text-input";
import buttonUpImage from "./buttonUp.png";
import buttonDownImage from "./buttonDown.png";
import TeamMembersCollection from "../team-members-collection";

class TeamCollectionElement extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            teamPassword: "",
            isExpanded: false
        }
    }

    getName() {
        return "Команда " + (this.props.idInGame !== undefined ? this.props.idInGame.toString() + " :" : "") + this.props.name;
    }

    render() {

        const {onSubmit = (teamName, password) => {}, members} = this.props;

        let res;
        let image = buttonUpImage;

        if (this.state.isExpanded) {
            image = buttonDownImage;
            res = <TeamMembersCollection
                items={members}
                style={{marginRight: "20px", marginLeft: "20px", marginTop: "-20px", marginBottom: "-20px"}}
                ulstyle={{paddingTop: "20px", paddingBottom: "20px", marginBottom: "0",
                    fontSize: "22px", listStyle: "none"}}
            />
        }

        let input;

        if (!this.props.isReadOnly) {
            input = (<DefaultTextInput
                style={{margin: "0 10px 0 10px", float: "right"}}
                onChange={(text) => this.setState(prevState => {
                    return {teamPassword: text}
                })}
                onKeyDown={(event) => {
                    if (event.key === 'Enter') {
                        event.preventDefault();
                        event.stopPropagation();
                        onSubmit(this.props.name, this.state.teamPassword);
                    }
                }}
                onFocus={(ev) => {
                    ev.stopPropagation();
                }}
                onClick={(ev) => {
                    ev.stopPropagation();
                }}
            />);
        }

        return (
            <div onClick={(ev) => {

                this.setState(prevState => {
                    return {isExpanded: !prevState.isExpanded}
            })}}
                 style={{paddingBottom: "20px"}}
            >
            <div className={"item-container"}>
                <div className={"row"}>
                    <div className={"col-6"} style={{fontSize: "28px"}}>{this.getName()}</div>
                    <div className={"col-5"}>
                        {input}
                    </div>
                    <div style={{padding:"0px"}} className={"col-1"}>
                        <div style={{transform: "scale(0.3) translate(-30px, 0)"}}>
                            <button style={{
                                border: "none",
                                backgroundColor: "Transparent",
                                marginLeft: "15px"
                            }}><img src={image} alt={"unwind"}/></button>
                        </div>
                    </div>
                </div>
            </div>
                {res}
            </div>
        )
    }
}

class TeamCollection extends React.Component {

    render() {
        console.log(this.props.items);

        const {isReadOnly} = this.props;

        const items = this.props.items.map(item => {
            return (
                <div key={item.teamName}>
                    <TeamCollectionElement
                        name={item.teamName}
                        members={item.teamMembers}
                        idInGame={item.idInGame}
                        onSubmit={this.props.onSubmit}
                        isReadOnly={isReadOnly}
                    />
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
