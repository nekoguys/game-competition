import React from "react";

import "./root.css";
import CompetitionResultsTable from "../../competition-process-teacher/results-table";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import ReadonlyMessagesContainer from "../messages";
import ApiHelper from "../../../../helpers/api-helper";
import TeamCollection from "../../../join-competition/join-competition-player-form/team-collection";

class EndedCompetitionResultsRoot extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: "name",
            teams: [{teamMembers: ["a@emal", "b@emal"], teamName: "abbra", teamIdInGame: 1},
                {teamMembers: ["c@emal", "d@emal"], teamName: "tutu", teamIdInGame: 2}],
            produced: {1: {1: 10, 2: 20}, 2: {1: 30, 2: 40}},
            prices: {1: 6, 2: 5},
            results: {1 : {1: -10, 2: -20}},
            messages: [{message: "message", dateStr: "11:52pm"}],
            teamsOrder: [2, 1]
        }
    }

    componentDidMount() {
        this.fetchResults();
    }

    fetchResults() {
        const {pin} = this.props.match.params;
        ApiHelper.competitionAllResults(pin).then(resp => {
            if (resp.status >= 300) {
                resp.text().then(el => console.log(el));
            }
            return resp.json();
        }).then(jsonBody => {
            const messages = jsonBody.messages.map(elem => {
                const date = new Date(elem.sendTime * 1000);

                const dateStr = date.toLocaleDateString("en-US", {
                    hour: 'numeric',
                    minute: 'numeric',
                    day: 'numeric',
                    month: 'short',
                });

                return {
                    message: elem.message,
                    dateStr: dateStr,
                    timestamp: elem.sendTime
                };
            });
            console.log({jsonBody});

            let teams = jsonBody.teams;
            teams = teams.sort((lhs, rhs) => lhs.idInGame - rhs.idInGame);

            let orderedTeams = [];

            teams.forEach((val, ind) => {
                orderedTeams.push(teams[ind]);
            });

            console.log({teams});

            this.setState({
                competitionName: jsonBody.competitionName,
                teams: orderedTeams,
                teamsOrder: jsonBody.teamsOrderInDecreasingByTotalPrice,
                produced: jsonBody.produced,
                results: jsonBody.income,
                prices: jsonBody.prices,
                messages: messages
            })
        })
    }

    render() {
        const {pin} = this.props.match.params;
        const {competitionName} = this.state;

        const res = (
            <div style={{marginTop: "-15px"}}>
                <CompetitionResultsTable style={{width: "100%"}} teamsCount={this.state.teams.length}
                                     roundsCount={Object.keys(this.state.prices).length}
                                     answers={this.state.produced}
                                     results={this.state.results}
                                     prices={this.state.prices}
                                     teamsPermutation={this.state.teamsOrder}
                />
            </div>
        );

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "80px"}}>
                    <div style={{fontSize: "26px"}}>
                        <div style={{textAlign: "center"}}>
                            {"Игра: " + competitionName + ", ID: " + pin}
                        </div>
                        <div style={{textAlign: "center"}}>
                            {"Закончена"}
                        </div>
                    </div>
                </div>
                <div>
                    <div className={"game-state-holder"}>
                        {res}
                        <div style={{paddingTop: "20px"}}>
                            <ReadonlyMessagesContainer messages={this.state.messages}/>
                        </div>
                        <div style={{paddingTop: "40px"}}>
                            <div style={{width: "70%", minWidth: "200px", margin: "0 auto"}}>
                            <TeamCollection items={this.state.teams} isReadOnly={true}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default EndedCompetitionResultsRoot;
