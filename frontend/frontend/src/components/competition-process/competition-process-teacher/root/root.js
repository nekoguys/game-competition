import React from "react";

import "./root.css";
import {NavbarHeaderWithFetcher as NavbarHeader} from "../../../app/app";
import CompetitionProcessTeacherBody from "../body";
import withRedirect from "../../../../helpers/redirect-helper";
import showNotification from "../../../../helpers/notification-helper";
import {withTranslation} from "react-i18next";

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
               showNotification(this).success("Game over", "Game over", 2500);

               setTimeout(() => {
                   const {pin} = this.props.match.params;
                   this.props.history("/competitions/results/" + pin);
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
                            {this.props.i18n.t("competition_process.teacher.root.game") + competitionName}
                        </div>
                        <div style={{textAlign: "center"}}>
                            {"ID: " + pin}
                        </div>
                    </div>


                    <div>
                        <CompetitionProcessTeacherBody showNotification={this.props.showNotification} onEndCallback={this.onRedirectToResultsPage} pin={pin} updateCompetitionNameCallback={this.updateCompetitionNameCallback}/>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(withRedirect(CompetitionProcessTeacherRootComponent));
