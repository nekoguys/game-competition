import React from "react";
import "./competition-collection.css";

import DefaultSubmitButton from "../../../common/default-submit-button";
import {withRouter} from "react-router-dom";
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
            res = <div style={{margin: "auto 0", display: "inline-block"}}>{this.stateMapper(state)}</div>
        }

        let button;
        if (owned)
            button = <DefaultSubmitButton text={this.props.i18n.t('competition_history.clone')} onClick={(ev) => {
                console.log(this);
                this.props.history.push('/competitions/create/', {initialState: this.props.item});
                ev.stopPropagation();
            }}/>

        return <div className={"item-element-container"} onClick={() => {
            console.log("outer div click");
            onItemClickCallback(this.props.item);
        }}>
            <div className={"row"}>
                <div className={"col-7 center-text"} style={{textAlign: "center"}}>{name}</div>
                <div className={"col-3 center-text"} style={{textAlign: "center"}}>
                    <div style={{padding: "10px", minHeight: "68px"}} className={"center-text"}>
                    {res}

                    </div>
                </div>
                {
                    !this.props.isAnyCloneable ? undefined : // и так пойдёт
                    <div className={"col-2 flex-center-vertically"}>
                        <div style={{margin: "auto 0"}} className={""}>
                            <div style={{marginBottom: "-10px"}}>
                                {button}
                            </div>
                        </div>
                    </div>
                }
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
                <div className={"row"} style={{textAlign: "center"}}>
                    <div className={"col-7"}>{i18n.t('competition_history.name')}</div>
                    <div className={"col-3"}>{i18n.t('competition_history.status')}</div>
                    {this.props.isAnyCloneable ? <div className={"col-2"}/> : undefined}
                </div>
                {elements}
            </div>
        )
    }
}

export default withTranslation('translation')(withRouter(CompetitionCollection));