import React from "react";

import "./competition-params.css"
import DefaultTextInput from "../../common/default-text-input";
import DefaultCheckboxButton from "../../common/default-checkbox-button";
import toSnakeCase from "../../../helpers/snake-case-helper";
import getValueForJsonObject from "../../../helpers/competition-params-helper";

class CompetitionParams {
    constructor({initialParams = {}}) {
        this.name = "";
        this.expensesFormula = "";
        this.demandFormula = "";
        this.maxTeamsAmount = "";
        this.maxTeamSize = "";
        this.roundsCount = "";
        this.roundLength = "";
        this.instruction = "";
        this.teamLossUpperbound = "";
        this.shouldShowStudentPreviousRoundResults = false;
        this.shouldEndRoundBeforeAllAnswered = false;
        this.shouldShowResultTableInEnd = false;
        this.isAutoRoundEnding = false;
        this.showOtherTeamsMembers = true;

        for (const key of Object.keys(initialParams)) {
            this[key] = initialParams[key];
        }
    }


    toJSONObject() {
        let jsonObj = {};

        Object.keys(this).forEach(key => {
            jsonObj[toSnakeCase(key)] = getValueForJsonObject(key, this[key]);
        });

        return jsonObj;
    }

}

class CompetitionParamsForm extends React.Component {

    constructor(props) {
        super(props);

        const {initialState} = props;
        this.formState = new CompetitionParams({initialParams: initialState});

        let demandFormula_a = "";
        let demandFormula_b = "";
        if ('demandFormula' in this.formState) {
            const demandSpl = this.formState.demandFormula.split(';');
            if (demandSpl.length === 2) {
                demandFormula_a = demandSpl[0];
                demandFormula_b = demandSpl[1];
            }
        }

        this.demand = {
            demandFormula_a: demandFormula_a,
            demandFormula_b: demandFormula_b
        };
        this.onFormStateUpdated();
    }

    updateFormStateField(fieldName, value) {
        this.formState[fieldName] = value;
        console.log(this.formState);
        console.log(this.formState.toJSONObject());

        this.onFormStateUpdated();
    }

    updateDemand(fieldName, value) {
        this.demand[fieldName] = value;
        this.updateFormStateField("demandFormula", this.demand.demandFormula_a + ";" + this.demand.demandFormula_b)
    }

    onFormStateUpdated = () => {
        const {onFormStateUpdated} = this.props;

        onFormStateUpdated(this.formState);
    };

    render() {
        const defaultTextInputStyle = {
            width: "100%",
            display: "block",
            lineHeight: "1.5",
            padding: ".375rem 0.75rem",
            margin: "0"
        };

        const formulaInputStyle = {
            lineHeight: "1.5",
            padding: ".375rem 0.75rem",
            margin: "0"
        }

        const checkboxButtonStyle = {
            margin: "0 auto",
            borderRadius: "10px",
            width: "70%",
            padding: "5px",
        };

        const checkBoxButtonLabelStyle = {
            fontSize: "26px"
        };
        const demandFormulaSplit = this.formState.demandFormula.split(';')
        let demandFormula_a;
        let demandFormula_b;
        if (demandFormulaSplit.length === 2) {
            demandFormula_a = demandFormulaSplit[0];
            demandFormula_b = demandFormulaSplit[1];
        }

        return (

                <form>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"} style={{float: "right"}}>Название игры</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} placeholder={"Например, конкуренция на рынке пшеницы"} defaultText={this.formState.name}
                            onChange={(value) => this.updateFormStateField("name", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label  text-right"}>Формула затрат</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} placeholder={"1;2;3 - x^2+2x+3; x - объем выпуска"} defaultText={this.formState.expensesFormula}
                            onChange={(value) => this.updateFormStateField("expensesFormula", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row "}>
                        <label className={"col-sm-3 col-form-label text-right justify-content-end"}>Формула спроса</label>
                        <div className={'col-sm-9 row'} style={{marginLeft: "0"}}>
                            <DefaultTextInput style={{maxWidth: "250px"}} additionalClassNames={'formula'} placeholder={"Свободный коэффициент"} defaultText={demandFormula_a}
                            onChange={(value) => this.updateDemand("demandFormula_a", value)}/>
                            <div style={{position: "relative", minWidth: "2rem"}}>
                                <p style={{position: "absolute", top: "50%", transform: "translate(0, -60%)", margin: "0"}}>a -</p>
                            </div>

                            <DefaultTextInput style={{maxWidth: "250px"}} additionalClassNames={'formula'} placeholder={"Коэффициент при цене"} defaultText={demandFormula_b}
                                                  onChange={(value) => this.updateDemand("demandFormula_b", value)}/>
                            <div style={{position: "relative", minWidth: "2rem"}}>
                                <p style={{position: "absolute", top: "50%", transform: "translate(0, -60%)", margin: "0"}}>bP</p>
                            </div>
                            <div className={"col"}></div>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Максимальные убытки</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} placeholder={"Модуль максимальных убытков"} defaultText={this.formState.teamLossUpperbound}
                                              onChange={(value) => this.updateFormStateField("teamLossUpperbound", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Кол-во команд</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}
                                              defaultText={this.formState.maxTeamsAmount}
                                              onChange={(value) => this.updateFormStateField("maxTeamsAmount", value)}
                            />
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Игроков в команде</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.maxTeamSize}
                            onChange={(value) => this.updateFormStateField("maxTeamSize", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Кол-во раундов</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.roundsCount}
                            onChange={(value) => this.updateFormStateField("roundsCount", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Продолжительность раунда</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} placeholder={"В секундах"} defaultText={this.formState.roundLength}
                            onChange={(value) => this.updateFormStateField("roundLength", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Инструкция для студентов</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.instruction}
                            onChange={(value) => this.updateFormStateField("instruction", value)}/>
                        </div>
                    </div>

                    <div className={"form-group row"} style={{marginTop: "60px"}}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Показывать студентам результаты предыдущих раундов"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}
                                               checked={this.formState.shouldShowStudentPreviousRoundResults}
                                               onChange={(value) => this.updateFormStateField("shouldShowStudentPreviousRoundResults", value)}
                        />
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Не завершать раунд, пока все не отправили решение"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}
                                               checked={this.formState.shouldEndRoundBeforeAllAnswered}
                                               onChange={(value) => this.updateFormStateField("shouldEndRoundBeforeAllAnswered", value)}
                        />
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"После окончания показывать результаты всех команд"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}
                                               checked={this.formState.shouldShowResultTableInEnd}
                                               onChange={(value) => this.updateFormStateField("shouldShowResultTableInEnd", value)}
                        />
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Автоматическое проведение игры"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}
                                               checked={this.formState.isAutoRoundEnding}
                                               onChange={(value) => this.updateFormStateField("isAutoRoundEnding", value)}
                        />
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Показывать составы команд студентам"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}
                                               checked={this.formState.showOtherTeamsMembers}
                                               onChange={(value) => this.updateFormStateField("showOtherTeamsMembers", value)}
                        />
                    </div>



                </form>
       )
    }
}

export default CompetitionParamsForm;