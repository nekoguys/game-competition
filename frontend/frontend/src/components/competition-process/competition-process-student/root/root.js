import React from "react";

import "./root.css";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import StudentResultsTable from "../results-table";
import SendAnswer from "../send-answer";
import MessagesContainer from "../messages";
import DescriptionHolder from "../description";

class CompetitionProcessStudentRoot extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: 'sample',
            roundsCount: 17,
            prices: {1: 15, 2: 20},
            answers: {1: 100, 2: 200},
            results: {1: -20, 2: 30},
            currentRound: 5,
            timeTillRoundEnd: 55,
            teamName: "teamName",
            teamIdInGame: 1,
            messages: [{message: "sampleMessage", dateStr: "11:02"}],
            description: ""
        }
    }
    render() {
        let instr = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do \n" +
            "eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
            "Nisl vel pretium lectus quam.  vulputate odio ut enim.\n" +
            "Nunc sed id semper risus in hendrerit. \n" +
            "\n";
        const {competitionName, roundsCount, prices, answers, results, currentRound, timeTillRoundEnd} = this.state;

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "80px"}}>
                    <div style={{textAlign: "center", fontSize: "26px"}}>
                        {"Игра: " + competitionName}
                    </div>
                    <div className={"game-state-holder"}>
                        <div className={"row justify-content-between"}>
                            <div className={"col-4"}>
                                <div>
                                    <div style={{textAlign: "center", fontSize: "23px"}}>
                                        {"Текущий раунд: " + currentRound}
                                    </div>
                                </div>
                            </div>
                            <div className={"col-4"}>
                                <div style={{textAlign: "center", fontSize: "23px"}}>
                                    {"До конца раунда: " + timeTillRoundEnd + "сек"}
                                </div>
                            </div>
                        </div>
                        <div style={{textAlign: "center", fontSize: "26px"}}>
                            {"Команда " + this.state.teamIdInGame + ": " + this.state.teamName}
                        </div>
                        <div style={{paddingTop: "10px", width: "100%"}}>
                            <StudentResultsTable roundsCount={roundsCount} prices={prices} answers={answers} results={results}/>
                        </div>
                        <div style={{paddingTop: "30px"}}>
                            <SendAnswer/>
                        </div>
                        <div style={{paddingTop: "30px"}}>
                            <MessagesContainer messages={this.state.messages}/>
                        </div>
                        <div style={{paddingTop: "30px"}}>
                            <DescriptionHolder instruction={instr}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default CompetitionProcessStudentRoot;
