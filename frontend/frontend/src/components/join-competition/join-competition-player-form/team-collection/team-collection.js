import React from "react";
import "./team-collection.css";
import DefaultTextInput from "../../../common/default-text-input";
import buttonUpImage from "./buttonUp.png";
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
        return "Команда " + this.props.name;
    }

    render() {

        const {onSubmit = (teamName, password) => {}, members} = this.props;

        let res;

        if (this.state.isExpanded) {
            res = <TeamMembersCollection
                items={members}
                style={{marginRight: "20px", marginLeft: "20px"}}
                ulstyle={{paddingTop: "20px", paddingBottom: "20px", marginBottom: "0"}}
            />
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
                        <DefaultTextInput
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
                        />
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
                {res}
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
                    <TeamCollectionElement
                        name={item.teamName}
                        members={item.teamMembers}
                        onSubmit={this.props.onSubmit}
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
