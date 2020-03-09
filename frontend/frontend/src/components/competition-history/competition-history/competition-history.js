import React from "react";
import NavbarHeader from "../navbar-header";
import {NotificationContainer, NotificationManager} from "react-notifications";
import CompetitionCollection from './competition-collection';
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";
import DefaultSubmitButton from "../../common/default-submit-button";

import "./competition-history.css";

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
                <div>
                <NavbarHeader/>
                </div>
                <div style={{padding: "80px 40px 0px 40px"}}>
                    <div style={{fontSize: "28px", padding: "40px 0", textAlign: "center"}}>
                        {"Последние игры"}
                    </div>
                    <div className={"collection-holder"} style={{margin: "0 auto"}}>
                        <CompetitionCollection items={this.state.items}/>

                        <div style={{paddingTop: "30px"}}>
                            <div className={"row justify-content-center"}>

                                <div style={{flex: "0 0 12.5%", paddingRight: "15px"}}>
                                <DefaultSubmitButton text={"Вперёд"} onClick={() => {
                                    this.updateHistory( this.state.currentStart + 4, 4);
                                }} style={{padding: "10px 20px", width: "100%"}}
                                   isDisabled={this.state.items.length === 0}/>
                                </div>

                                <div style={{flex: "0 0 12.5%", paddingLeft: "15px"}}>
                                <DefaultSubmitButton text={"Назад"} onClick={() => {
                                    this.updateHistory( this.state.currentStart - 4, 4);
                                }} style={{padding: "10px 20px", width: "100%"}} isDisabled={this.state.currentStart === 0}/>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>
                <NotificationContainer/>
            </div>
        )
    }
}

export default withRouter(CompetitionHistory);