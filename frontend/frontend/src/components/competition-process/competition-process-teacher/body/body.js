import React from "react";

import "./body.css"
import DefaultSubmitButton from "../../../common/default-submit-button";
import CompetitionResultsTable from "../results-table";
import MessagesContainer from "../messages";

class CompetitionProcessTeacherBody extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentRoundNumber: 0,
            timeTillRoundEnd: 0,
            isCurrentRoundEnded: false
        }
    }

    render() {
        let res = (<CompetitionProcessTeacherActive
            round={this.state.currentRoundNumber}
            timeLeft={this.state.timeTillRoundEnd}
            isRoundEnded={this.state.isCurrentRoundEnded}
        />);

        return (
            <div className={"game-state-holder"}>
                {res}
            </div>
        );
    }
}

class CompetitionProcessTeacherActive extends React.Component {
    render() {
        const {round, timeLeft, isRoundEnded} = this.props;

        const rightButtonText = isRoundEnded ? "Начать новый раунд" : "Закончить раунд";

        return (
            <div>
                <div className={"row justify-content-between"}>
                    <div className={"col-4"}>
                        <div>
                            <div style={{textAlign: "center", fontSize: "23px"}}>
                                {"Текущий раунд: " + round}
                            </div>
                            <div style={{paddingTop: "20px"}}>
                                <DefaultSubmitButton text={"Начать раунд заново"}/>
                            </div>
                        </div>
                    </div>
                    <div className={"col-4"}>
                        <div style={{textAlign: "center", fontSize: "23px"}}>
                            {"До конца раунда: " + timeLeft + "сек"}
                        </div>
                        <div style={{paddingTop: "20px"}}>
                            <DefaultSubmitButton text={rightButtonText}/>
                        </div>
                    </div>
                </div>
                <div style={{paddingTop: "20px"}}>
                    <CompetitionResultsTable style={{width: "100%"}} teamsCount={10} roundsCount={6} />
                </div>
                <div style={{paddingTop: "20px"}}>
                    <MessagesContainer messages={[{message: "msg", dateStr: "11:52"}, {message: "msg2", dateStr: "11:52"}]}/>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessTeacherBody;
