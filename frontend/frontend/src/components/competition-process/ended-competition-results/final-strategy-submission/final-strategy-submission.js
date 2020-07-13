import React from "react";

import "./final-strategy-submission.css";
import ApiHelper from "../../../../helpers/api-helper";
import StrategySubmissionComponent from "../../strategy-submission";
import showNotification from "../../../../helpers/notification-helper";
import DefaultSubmitButton from "../../../common/default-submit-button";
import withAuthenticated from "../../../../helpers/with-authenticated";

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
                    showNotification(this).success(jsonBody.message, "Успех", 2500);
                } else {
                    showNotification(this).error(jsonBody, "Ошибка", 4000);
                }
            })
        })
    };

    onSubmitButtonClicked = () => {
        const {pin} = this.props.match.params;
        this.props.history.push("/competitions/results/" + pin);
    };

    render() {
        return (
            <div>
                <div style={{textAlign: "center", fontSize: "26px"}}>
                    {"Игра: " + this.state.competitionName}
                </div>
                <div className={"game-state-holder"}>
                    <div style={{textAlign: "center", fontSize: "26px"}}>
                        {"Команда " + this.state.teamIdInGame + ": " + this.state.teamName}
                    </div>
                    <div style={{paddingTop: "30px"}}>
                        <StrategySubmissionComponent defaultText={this.state.fetchedStrategy} isExpanded={true} onSubmit={this.submitStrategy}/>
                    </div>
                    <div style={{paddingTop: "30px"}} className={"row justify-content-center"}>
                        <div style={{width: "20%", minWidth: "15em"}}>
                            <DefaultSubmitButton text={"Продолжить"} onClick={this.onSubmitButtonClicked}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default withAuthenticated(FinalStrategySubmissionComponent);
