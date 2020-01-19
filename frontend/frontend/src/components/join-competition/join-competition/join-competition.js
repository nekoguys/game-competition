import React from "react";
import "./join-competition.css"
import JoinCompetitionForm from "../join-competition-form";
import DefaultCheckboxButtonGroup from "../../common/default-checkbox-button-group";

class JoinCompetition extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            currentPage: "capitain"
        }
    }

    onCreateTeamPageClick = () => {
        console.log("createTeamPageClick");
    };

    onJoinTeamPageClick = () => {
        console.log("onJoinTeamPageClick");
    };

    render() {
        return (
            <div>
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
                <JoinCompetitionForm/>
            </div>
        )
    }
}

export default JoinCompetition;
