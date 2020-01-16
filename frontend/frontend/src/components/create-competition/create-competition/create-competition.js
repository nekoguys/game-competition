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
                    <div style={{margin: "0 auto", textAlign: "center", fontSize: "36px"}}>
                        <span>Создание Игры
                        </span>
                    </div>
                    <CompetitionParamsForm/>
                </div>
            </div>
        )
    }
}

export default CreateCompetition;