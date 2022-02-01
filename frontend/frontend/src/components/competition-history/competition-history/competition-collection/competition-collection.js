import React from "react";
import "./competition-collection.css";

import DefaultSubmitButton from "../../../common/default-submit-button";
import {withRouter} from "../../../../helpers/with-router";
import {withTranslation} from "react-i18next";

class CompetitionCollectionElement extends React.Component {
    stateMapper(state) {
        if (state === "Registration")
            return this.props.i18n.t('competition_history.list.registration');
        else if (state === "InProcess")
            return this.props.i18n.t('competition_history.list.in_process');
        else if (state === "Draft")
            return this.props.i18n.t('competition_history.list.draft');
        else if (state === "Ended")
            return this.props.i18n.t('competition_history.list.ended');
        else
            return "Неизвестно";
    }

    render() {
        const {name, state, lastUpdateTime, owned} = this.props.item;
        const {onItemClickCallback = (item) => {}} = this.props;

        let res;
        if (lastUpdateTime) {
            res = (
                <div>
                <div style={{margin: "auto 0", display: "inline-block"}}>{this.stateMapper(state)}</div>
                <div >{lastUpdateTime}</div>
                </div>
            )
        } else {
            res = <div>{this.stateMapper(state)}</div>
        }

        let button;
        let stateCloneSpacer;
        if (this.props.isAnyCloneable) {
            button = <div className={"clone-button"}>
                <DefaultSubmitButton text={this.props.i18n.t('competition_history.clone')} onClick={(ev) => {
                    console.log(this);
                    this.props.history('/competitions/create/', {initialState: this.props.item});
                    ev.stopPropagation();
                }}/>
            </div>
            stateCloneSpacer = <div className={"spacer-state-clone"}/>
        }
        const stateAdditionalClasses = owned ? "  state-text-with-clone-button" : "";
        return <div className={"item-element-container"} onClick={() => {
            console.log("outer div click");
            onItemClickCallback(this.props.item);
        }}>
            <div style={{display: "flex", height: "100%"}}>
                <div className={"game-name-text"}>{name}</div>
                <div className={"spacer-name-state"}/>
                <div className={"state-text state-text-table" + stateAdditionalClasses}>
                    {res}
                </div>
                {stateCloneSpacer}
                {button}
            </div>
        </div>
    }
}

class CompetitionCollection extends React.Component {
    render() {
        const {items, i18n} = this.props;
        console.log({items});

        const elements = items.map(item => {
            return <CompetitionCollectionElement i18n={i18n} onItemClickCallback={this.props.onHistoryItemClickCallback}
                    key={item.pin} item={item} history={this.props.history} isAnyCloneable={this.props.isAnyCloneable}/>
        });
        return (
            <div className={"collection-container"}>
                {elements}
            </div>
        )
    }
}

export default withTranslation('translation')(withRouter(CompetitionCollection));