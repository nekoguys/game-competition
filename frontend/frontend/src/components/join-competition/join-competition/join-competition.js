import React from "react";
import "./join-competition.css"
import JoinCompetitionForm from "../join-competition-captain-form";
import DefaultCheckboxButtonGroup from "../../common/default-checkbox-button-group";
import NavbarHeader from "../../competition-history/navbar-header/navbar-header";
import toSnakeCase from "../../../helpers/snake-case-helper";
import JoinCompetitionPlayerForm from "../join-competition-player-form";

class JoinCompetition extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            currentPage: "captain"
        }
    }

    onCreateTeamPageClick = () => {
        console.log("createTeamPageClick");
        this.setState(() => {
            return {currentPage: "captain"};
        })
    };

    onJoinTeamPageClick = () => {
        console.log("onJoinTeamPageClick");
        this.setState(() => {
            return {currentPage: "player"};
        })
    };

    onCreateTeamClick = (formState) => {
        let obj = {};
        Object.keys(formState).forEach(key => {
            obj[toSnakeCase(key)] = formState[key];
        });
        console.log(obj);
    };

    render() {
        let res;
        if (this.state.currentPage === "captain") {
            res = <JoinCompetitionForm onFormSubmit={this.onCreateTeamClick}/>
        } else {
            res = <JoinCompetitionPlayerForm/>
        }

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
            <div style={{paddingTop: "90px"}}>
                <div className={"d-flex"}>
                    <div style={{margin: "0 auto", width: "26%"}}>
                        <DefaultCheckboxButtonGroup
                            choices={["Как капитан", "Как участник"]}
                            buttonStyle={{width: "51%", fontSize: "1.3rem",
                                textOverflow: "ellipsis", overflow: "hidden"}}
                            style={{width: "100%"}}
                            onChoiceChanged={[
                                this.onCreateTeamPageClick,
                                this.onJoinTeamPageClick
                            ]}/>
                    </div>
                </div>
                {res}
            </div>
            </div>
        )
    }
}

export default JoinCompetition;
