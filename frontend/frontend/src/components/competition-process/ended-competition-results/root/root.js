import React from "react";

import "./root.css";
import CompetitionResultsTable from "../../competition-process-teacher/results-table";
import NavbarHeader from "../../../competition-history/navbar-header/navbar-header";
import ReadonlyMessagesContainer from "../messages";
import ApiHelper from "../../../../helpers/api-helper";
import TeamCollection from "../../../join-competition/join-competition-player-form/team-collection";
import DescriptionHolder from "../../competition-process-student/description";
import {isTeacher} from "../../../../helpers/role-helper";
import {withTranslation} from "react-i18next";

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
            teamsOrder: [2, 1],
            strategy: {}
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
                messages: messages,
                instruction: jsonBody.instruction,
                strategy: jsonBody.strategyHolders
            })
        })
    }

    render() {

        const {i18n} = this.props;

        const {pin} = this.props.match.params;
        const {competitionName} = this.state;

        const isTeacher_ = isTeacher();
        console.log({isTeacher_});
        console.log({strategy: this.state.strategy});

        const res = (
            <div style={{marginTop: "-15px"}}>
                <CompetitionResultsTable style={{width: "100%"}} teamsCount={this.state.teams.length}
                                     roundsCount={Object.keys(this.state.prices).length}
                                     answers={this.state.produced}
                                     results={this.state.results}
                                     prices={this.state.prices}
                                     teamsPermutation={this.state.teamsOrder}
                                     strategy={this.state.strategy}
                                     showStrategy={isTeacher_}
                />
            </div>
        );

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <div style={{fontSize: "26px"}}>
                        <div style={{textAlign: "center"}}>
                            {i18n.t('competition_results.game') + competitionName + ", ID: " + pin}
                        </div>
                        <div style={{textAlign: "center"}}>
                            {i18n.t('competition_results.ended')}
                        </div>
                    </div>
                </div>
                <div style={{paddingBottom: "20px"}}>
                    <div className={"game-state-holder"}>
                        {res}
                        <div style={{paddingTop: "20px"}}>
                            <ReadonlyMessagesContainer messages={this.state.messages}/>
                        </div>
                        <div style={{paddingTop: "40px"}}>
                            <div style={{width: "70%", minWidth: "200px", margin: "0 auto"}}>
                            <TeamCollection i18n={i18n} items={this.state.teams} isReadOnly={true}
                                            showStrategy={isTeacher_} strategy={this.state.strategy}
                            />
                            </div>
                        </div>
                        <div>
                            <DescriptionHolder instruction={this.state.instruction}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default withTranslation('translation')(EndedCompetitionResultsRoot);
