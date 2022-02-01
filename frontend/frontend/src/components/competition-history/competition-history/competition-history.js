import React from "react";
import NavbarHeader from "../navbar-header";
import {NotificationContainer} from "react-notifications";
import CompetitionCollection from './competition-collection';
import DefaultSubmitButton from "../../common/default-submit-button";

import "./competition-history.css";
import "../../../helpers/common.css";
import withAuthenticated from "../../../helpers/with-authenticated";
import {withTranslation} from "react-i18next";

class CompetitionHistory extends React.Component {
    static itemsPerPage = 4;

    constructor(props) {
        super(props);

        this.state = {
            itemsLoaded: 0,
            items: [],
            isAnyCloneable: true
        }
    }

    componentDidMount() {
        this.updateHistory(CompetitionHistory.itemsPerPage)
    }

    updateHistory(delta) {
        this.props.fetchers.history(this.state.itemsLoaded, delta).then(resp => {
            this.setState(prevState => {
                return {
                    items: prevState.items.concat(resp),
                    itemsLoaded: prevState.itemsLoaded + delta,
                    isAnyCloneable: prevState.items.concat(resp).some((x) => x.owned)
                }
            }, () => this.scrollToBottom())
        })
    }

    onHistoryItemClickCallback = (item) => {
        let isTeacher = false;
        if (window.localStorage.getItem("roles").includes("TEACHER")) {
            isTeacher = true;
        }
        console.log({isTeacher});
        if (item.state.toLowerCase() === "registration") {
            if (isTeacher) {
                this.props.history('/competitions/after_registration_opened/' + item.pin);
            } else {
                this.props.history('/competitions/waiting_room/' + item.pin);
            }
        } else if (item.state.toLowerCase() === "inprocess") {
            if (isTeacher) {
                this.props.history('/competitions/process_teacher/' + item.pin);
            } else {
                this.props.history('/competitions/process_captain/' + item.pin);
            }
        } else if (item.state.toLowerCase() === "ended") {
            if (isTeacher || item.shouldShowResultTableInEnd) {
                this.props.history('/competitions/results/' + item.pin);
            }
        } else if (item.state.toLowerCase() === "draft") {
            if (isTeacher) {
                this.props.history('/competitions/draft_competition/' + item.pin, {initialState: item})
            }
        }
    };

    scrollToBottom = () => {
        this.competitionsEnd.scrollIntoView({behavior: "smooth"});
    };

    render() {
        const {i18n} = this.props;
        return (
            <div>
                <div>
                <NavbarHeader/>
                </div>
                <div className={"below-navbar root-container"}>
                    <div className={"page-title title"}>
                        {i18n.t('competition_history.last_games')}
                    </div>
                    <div className={"collection-holder"} style={{margin: "0 auto"}}>
                        <CompetitionCollection items={this.state.items} onHistoryItemClickCallback={this.onHistoryItemClickCallback} isAnyCloneable={this.state.isAnyCloneable}/>
                        <div className={"more-button-container"}>
                            <DefaultSubmitButton additionalClasses={["more-button"]} text={i18n.t('competition_history.more')} onClick={() => {
                                this.updateHistory(CompetitionHistory.itemsPerPage);
                            }}/>
                        </div>
                    </div>
                </div>
                <div style={{ float:"left", clear: "both" }}
                     ref={(el) => { this.competitionsEnd = el; }}>
                </div>
                <NotificationContainer/>
            </div>
        )
    }
}

export default withTranslation('translation')(withAuthenticated(CompetitionHistory));