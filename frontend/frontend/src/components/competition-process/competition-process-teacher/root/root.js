import React from "react";

import "./root.css";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import CompetitionProcessTeacherBody from "../body";
import {withRouter} from "react-router-dom";
import {NotificationContainer, NotificationManager} from "react-notifications";

class CompetitionProcessTeacherRootComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: "",
            didEnd: false
        }
    }

    updateCompetitionNameCallback = (name) => {
        this.setState({competitionName: name});
    };

    onRedirectToResultsPage = () => {
        this.setState(prevState => {
           if (!prevState.didEnd) {
               NotificationManager.success("Игра закончена", "Игра закончена", 2500);

               setTimeout(() => {
                   const {pin} = this.props.match.params;
                   this.props.history.push("/competitions/results/" + pin);
               }, 2500);
               return {didEnd: true};
           }
        });
    };

    render() {
        const {pin} = this.props.match.params;
        const {competitionName} = this.state;

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "80px"}}>
                    <div style={{fontSize: "26px"}}>
                        <div style={{textAlign: "center"}}>
                            {"Игра: " + competitionName}
                        </div>
                        <div style={{textAlign: "center"}}>
                            {"ID: " + pin}
                        </div>
                    </div>


                    <div>
                        <CompetitionProcessTeacherBody onEndCallback={this.onRedirectToResultsPage} pin={pin} updateCompetitionNameCallback={this.updateCompetitionNameCallback}/>
                    </div>
                </div>
                <div>
                    <NotificationContainer/>
                </div>
            </div>
        )
    }
}

export default withRouter(CompetitionProcessTeacherRootComponent);
