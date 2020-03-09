import React from "react";
import NavbarHeader from "../navbar-header";
import {NotificationContainer, NotificationManager} from "react-notifications";
import CompetitionCollection from './competition-collection';
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";
import DefaultSubmitButton from "../../common/default-submit-button";

class CompetitionHistory extends React.Component {
    static itemsPerPage = 4;

    constructor(props) {
        super(props);

        this.state = {
            itemsLoaded: 0,
            items: []
        }
    }

    componentDidMount() {
        this.updateHistory(CompetitionHistory.itemsPerPage)
    }

    updateHistory(delta) {
        ApiHelper.createdCompetitions(this.state.itemsLoaded, delta).then(resp => {
            if (resp.status < 300)
                resp.json().then(json => {
                    console.log(json);
                    this.setState(prevState => {
                        return {items: prevState.items.concat(json), itemsLoaded: prevState.itemsLoaded + delta}
                    });
                })
        })
    }

    render() {
        return (
            <div>
                <NavbarHeader/>
                <CompetitionCollection items={this.state.items}/>
                <DefaultSubmitButton text={"Ещё"} onClick={() => {
                    this.updateHistory(CompetitionHistory.itemsPerPage);
                }} style={{padding: "10px 20px"}}/>
                <NotificationContainer/>
            </div>
        )
    }
}

export default withRouter(CompetitionHistory);