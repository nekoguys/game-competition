import React from "react";
import "./competition-collection.css";

import DefaultSubmitButton from "../../../common/default-submit-button";
import {withRouter} from "../../../../helpers/with-router";
import {withTranslation} from "react-i18next";

class CompetitionCollectionElement extends React.Component {
    stateMapper(state) {
        if (state === "Registration")
            return {stateText: this.props.i18n.t('competition_history.list.registration')};
        else if (state === "InProcess")
            return {stateText: this.props.i18n.t('competition_history.list.in_process'), additionalClassName: " state-text-marked" };
        else if (state === "Draft")
            return {stateText: this.props.i18n.t('competition_history.list.draft')};
        else if (state === "Ended")
            return {stateText: this.props.i18n.t('competition_history.list.ended')};
        else
            return {stateText: "Неизвестно"};
    }

    render() {
        const {name, state, lastUpdateTime, isOwned} = this.props.item;
        const {onItemClickCallback = (item) => {}} = this.props;
        const {stateText, additionalClassName = ""} = this.stateMapper(state);

        let res;
        if (lastUpdateTime) {
            res = (
                <div>
                <div style={{margin: "auto 0", display: "inline-block"}}>{stateText}</div>
                <div >{lastUpdateTime}</div>
                </div>
            )
        } else {
            res = <div>{stateText}</div>
        }

        let button;
        let stateCloneSpacer;
        if (this.props.isAnyCloneable) {
            button = <div className={"clone-button"}>
                <DefaultSubmitButton text={this.props.i18n.t('competition_history.clone')} onClick={(ev) => {
                    console.log({competitionCollectionElement: this.props});
                    this.props.history('/competitions/create/', {state: {...this.props.item}});
                    ev.stopPropagation();
                }}/>
            </div>
            stateCloneSpacer = <div className={"spacer-state-clone"}/>
        }
        const stateAdditionalClasses = isOwned ? "  state-text-with-clone-button" : "";
        return <div className={"item-element-container"} onClick={() => {
            console.log("outer div click");
            onItemClickCallback(this.props.item);
        }}>
            <div style={{display: "flex", height: "100%"}}>
                <div className={"game-name-text"}>{name}</div>
                <div className={"spacer-name-state"}/>
                <div className={"state-text state-text-table" + stateAdditionalClasses + additionalClassName}>
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
