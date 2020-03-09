import React from "react";
import "./competition-collection.css";

import DefaultSubmitButton from "../../../common/default-submit-button";
import {withRouter} from "react-router-dom";

class CompetitionCollectionElement extends React.Component {
    constructor(props) {
        super(props);
    }

    stateMapper(state) {
        if (state === "Registration")
            return "Регистрация";
        else if (state === "InProcess")
            return "Запущено";
        else if (state === "Draft")
            return "Черновик";
        else if (state === "Ended")
            return "Завершено";
        else
            return "Неизвестно";
    }

    render() {
        const {name, state, lastUpdateTime} = this.props.item;

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

        return <div className={"item-element-container"}>
            <div className={"row"}>
                <div className={"col-7 center-text"} style={{textAlign: "center"}}>{name}</div>
                <div className={"col-3 center-text"} style={{textAlign: "center"}}>
                    <div style={{padding: "10px", minHeight: "68px"}} className={"center-text"}>
                    {res}

                    </div>
                </div>
                <div className={"col-2 flex-center-vertically"}>
                    <div style={{margin: "auto 0"}} className={""}>
                        <div style={{marginBottom: "-10px"}}>
                    <DefaultSubmitButton text={"Клонировать"} onClick={() => {
                        console.log(this);
                        this.props.history.push('/competitions/create/', {initialState: this.props.item});
                    }}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}

class CompetitionCollection extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {
        const {items} = this.props;
        console.log({items});

        const elements = items.map(item => {
            return <CompetitionCollectionElement key={item.pin} item={item} history={this.props.history}/>
        });

        return (
            <div className={"collection-container"}>
                <div className={"row"} style={{textAlign: "center"}}>
                    <div className={"col-7"}>Название</div>
                    <div className={"col-3"}>Статус</div>
                    <div className={"col-2"}/>
                </div>
                {elements}
            </div>
        )
    }
}

export default withRouter(CompetitionCollection);