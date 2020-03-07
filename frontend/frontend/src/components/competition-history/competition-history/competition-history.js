import React from "react";
import NavbarHeader from "../navbar-header";
import {NotificationContainer, NotificationManager} from "react-notifications";
import CompetitionCollection from './competition-collection';
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";

class CompetitionHistory extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            items: []
        }
    }

    componentDidMount() {
        ApiHelper.createdCompetitions(2, 1000).then(resp => {
            resp.json().then(json => {
                console.log(json);
                this.setState({items: json});
            })
        })
    }

    render() {
        const competitions = this.state.items.map(x => {
            return <div key={x.pin}>
                {x.name}
            </div>
        });

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>

                {competitions}

                <div>
                    <NotificationContainer/>
                </div>
            </div>
        )
    }
}

export default withRouter(CompetitionHistory);