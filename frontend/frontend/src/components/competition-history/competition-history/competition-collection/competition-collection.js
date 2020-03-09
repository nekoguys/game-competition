import React from "react";
import "./competition-collection.css";

import DefaultSubmitButton from "../../../common/default-submit-button";
import {withRouter} from "react-router-dom";

class CompetitionCollectionElement extends React.Component {
    constructor(props) {
        super(props);
        this.item = props.item;
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
        const {name, state, lastUpdateTime} = this.item;

        return <div className={"item-container"}>
            <div className={"row"}>
                <div className={"col-2"} style={{}}>{name}</div>
                <div className={"col-1"}>
                    <div className={"row"}>{this.stateMapper(state)}</div>
                    <div className={"row"}>{lastUpdateTime}</div>
                </div>
                <div className={"col-1"}>
                    <DefaultSubmitButton text={"Клонировать"} onClick={() => {
                        console.log(this);
                        this.props.history.push('/competitions/create/', {initialState: this.item});
                    }}/>
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

        const elements = items.map(item => {
            return <CompetitionCollectionElement key={item.pin} item={item} history={this.props.history}/>
        });

        return (
            <div className={"collection-container"} style={{paddingTop: "90px", width: "80%", margin: "0 auto"}}>
                <div className={"row"}>
                    <div className={"col-2"}>Название</div>
                    <div className={"col-1"}>Статус</div>
                    <div className={"col-1"}/>
                </div>
                {elements}
            </div>
        )
    }
}

export default withRouter(CompetitionCollection);