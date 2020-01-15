import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";
import CompetitionParamsForm from "../competition-params";

class CreateCompetition extends React.Component {
    render() {
        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <CompetitionParamsForm/>
                </div>
            </div>
        )
    }
}

export default CreateCompetition;