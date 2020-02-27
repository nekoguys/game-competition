import React from "react";
import {NotificationContainer} from "react-notifications";

import "./root.css";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import CompetitionProcessTeacherBody from "../body";

class CompetitionProcessTeacherRootComponent extends React.Component {
    render() {
        const {pin} = this.props.match.params;

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "80px"}}>
                    <div style={{fontSize: "26px"}}>
                        <div style={{textAlign: "center"}}>
                            {"Игра: " + "Название игры"}
                        </div>
                        <div style={{textAlign: "center"}}>
                            {"ID: " + pin}
                        </div>
                    </div>


                    <div>
                        <CompetitionProcessTeacherBody/>
                    </div>

                    <div>
                        <NotificationContainer/>
                    </div>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessTeacherRootComponent;
