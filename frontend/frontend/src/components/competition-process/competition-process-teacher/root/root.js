import React from "react";

import "./root.css";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import CompetitionProcessTeacherBody from "../body";
import withRedirect from "../../../../helpers/redirect-helper";

class CompetitionProcessTeacherRootComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: ""
        }
    }

    updateCompetitionNameCallback = (name) => {
        this.setState({competitionName: name});
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
                        <CompetitionProcessTeacherBody pin={pin} updateCompetitionNameCallback={this.updateCompetitionNameCallback}/>
                    </div>
                </div>
            </div>
        )
    }
}

export default withRedirect(CompetitionProcessTeacherRootComponent);
