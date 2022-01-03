import React from "react";

import "./final-strategy-submission.css";
import ApiHelper from "../../../../helpers/api-helper";
import StrategySubmissionComponent from "../../strategy-submission";
import showNotification from "../../../../helpers/notification-helper";
import DefaultSubmitButton from "../../../common/default-submit-button";
import withAuthenticated from "../../../../helpers/with-authenticated";

import {withTranslation} from "react-i18next";

class FinalStrategySubmissionComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            competitionName: "",
            teamIdInGame: "",
            teamName: "",
            fetchedStrategy: ""
        }
    }

    componentDidMount() {
        //super.componentDidMount();

        const {pin} = this.props.match.params;
        ApiHelper.studentCompetitionInfo(pin).then(resp => {
            if (resp.status >= 300) {
                return {success: false, json: resp.text()}
            } else {
                return {success: true, json: resp.json()}
            }
        }).then(resp => {
            resp.json.then(jsonBody => {
                if (resp.success) {
                    this.setState(prevState => {
                        return {
                            teamIdInGame: jsonBody.teamIdInGame,
                            teamName: jsonBody.teamName,
                            competitionName: jsonBody.name,
                            isCaptain: jsonBody.isCaptain,
                            fetchedStrategy: jsonBody.strategy
                        }
                    })
                }
            })
        })
    }

    submitStrategy = (strategy) => {
        const {pin} = this.props.match.params;
        ApiHelper.submitStrategy({strategy}, pin).then(resp => {
            if (resp.status < 300) {
                return {success: true, json: resp.json()}
            } else {
                return {success: false, json: resp.text()}
            }
        }).then(el => {
            el.json.then(jsonBody => {
                if (el.success) {
                    showNotification(this).success(jsonBody.message, "Success", 2500);
                } else {
                    showNotification(this).error(jsonBody, "Error", 4000);
                }
            })
        })
    };

    onSubmitButtonClicked = () => {
        const {pin} = this.props.match.params;
        this.props.history("/competitions/results/" + pin);
    };

    render() {

        const {i18n} = this.props;

        return (
            <div>
                <div style={{textAlign: "center", fontSize: "26px"}}>
                    {i18n.t('competition_results.game') + this.state.competitionName}
                </div>
                <div className={"game-state-holder"}>
                    <div style={{textAlign: "center", fontSize: "26px"}}>
                        {i18n.t('competition_results.team') + this.state.teamIdInGame + ": " + this.state.teamName}
                    </div>
                    <div style={{paddingTop: "30px"}}>
                        <StrategySubmissionComponent defaultText={this.state.fetchedStrategy} isExpanded={true} onSubmit={this.submitStrategy}/>
                    </div>
                    <div style={{paddingTop: "30px"}} className={"row justify-content-center"}>
                        <div style={{width: "20%", minWidth: "15em"}}>
                            <DefaultSubmitButton text={i18n.t('competition_results.continue')} onClick={this.onSubmitButtonClicked}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default withTranslation('translation')(withAuthenticated(FinalStrategySubmissionComponent));
