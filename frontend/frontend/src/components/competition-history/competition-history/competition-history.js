import React from "react";
import NavbarHeader from "../navbar-header";
import {NotificationContainer, NotificationManager} from "react-notifications";
import CompetitionCollection from './competition-collection';
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";
import DefaultSubmitButton from "../../common/default-submit-button";

class CompetitionHistory extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentStart: 0,
            items: []
        }
    }

    componentDidMount() {
        this.updateHistory(this.state.currentStart, 4)
    }

    updateHistory(start, amount) {
        ApiHelper.createdCompetitions(start, amount).then(resp => {
            resp.json().then(json => {
                console.log(json);
                this.setState({items: json, currentStart: start});
            })
        })
    }

    render() {
        return (
            <div>
                <NavbarHeader/>
                <CompetitionCollection items={this.state.items}/>
                <DefaultSubmitButton text={"Вперёд"} onClick={() => {
                    this.updateHistory( this.state.currentStart + 4, 4);
                }} style={{padding: "10px 20px"}}
                   isDisabled={this.state.items.length === 0}/>
                <DefaultSubmitButton text={"Назад"} onClick={() => {
                    this.updateHistory( this.state.currentStart - 4, 4);
                }} style={{padding: "10px 20px"}} isDisabled={this.state.currentStart === 0}/>
                <NotificationContainer/>
            </div>
        )
    }
}

export default withRouter(CompetitionHistory);