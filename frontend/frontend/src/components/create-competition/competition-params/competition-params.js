import React from "react";

import "./competition-params.css"
import DefaultTextInput from "../../common/default-text-input";
import DefaultCheckboxButton from "../../common/default-checkbox-button";
import DefaultSubmitButton from "../../common/default-submit-button";
import toSnakeCase from "../../../helpers/snake-case-helper";
import ApiHelper from "../../../helpers/api-helper";

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
        this.shouldShowStudentPreviousRoundResults = false;
        this.shouldEndRoundBeforeAllAnswered = false;
        this.shouldShowResultTableInEnd = false;

        for (const key of Object.keys(initialParams)) {
            this[key] = initialParams[key];
        }
    }

    getValueForJsonObject(fieldName, value) {
        const parseFormula = (formula) => {return formula.split(";").filter(x => x)};
        const identity = (val) => val;
        const rules = {
            expensesFormula: parseFormula,
            demandFormula: parseFormula,
            roundsCount: parseInt,
            roundLength: parseInt,
            maxTeamSize: parseInt,
            maxTeamsAmount: parseInt,
        };

        return (rules[fieldName] || identity)(value);
    }

    toJSONObject() {
        let jsonObj = {};

        Object.keys(this).forEach(key => {
            jsonObj[toSnakeCase(key)] = this.getValueForJsonObject(key, this[key]);
        });

        return jsonObj;
    }

}

class CompetitionParamsForm extends React.Component {

    constructor(props) {
        super(props);

        const {initialState} = props;
        this.formState = new CompetitionParams({initialParams: initialState});
    }

    updateFormStateField(fieldName, value) {
        this.formState[fieldName] = value;
        console.log(this.formState);
        console.log(this.formState.toJSONObject())
    }

    onSaveAsDraftClick = () => {
        let obj = {...this.formState.toJSONObject(), state: "draft"};
        ApiHelper.createCompetition(obj).then(response => {
            console.log(response);
            return response.json();
        }).catch(err => console.log(err)).then(bodyJson => {
            console.log(bodyJson);
        })
    };

    render() {
        const defaultTextInputStyle = {
            width: "100%",
            display: "block",
            lineHeight: "1.5",
            padding: ".375rem 0.75rem",
            margin: "0"
        };

        const checkboxButtonStyle = {
            margin: "0 auto",
            borderRadius: "10px",
            width: "70%",
            padding: "5px",
        };

        const checkBoxButtonLabelStyle = {
            fontSize: "26px"
        };

       return (
            <div className={"competition-form-holder"}>
                <form>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"} style={{float: "right"}}>Название игры</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.name}
                            onChange={(value) => this.updateFormStateField("name", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label  text-right"}>Формула затрат</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.expensesFormula}
                            onChange={(value) => this.updateFormStateField("expensesFormula", value)}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Формула спроса</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.demandFormula}
                            onChange={(value) => this.updateFormStateField("demandFormula", value)}/>
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
                        <label className={"col-sm-3 col-form-label text-right"}>Продолжительность рануда</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle} defaultText={this.formState.roundLength}
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

                    <div className={"form-group row"} style={{marginTop: "30px", marginLeft: "7.5%", marginRight: "7.5%"}}>
                        <div className={"mr-auto p-2"}>
                            <DefaultSubmitButton text={"Сохранить черновик"} style={{height: "100%", fontSize: "26px",
                                paddingTop: "15.5px", paddingBottom: "15.5px"}} onClick={this.onSaveAsDraftClick}/>
                        </div>
                            <div className={"p-2"}>
                                <DefaultSubmitButton text={"Открыть регистрацию"} style={{height: "100%", fontSize: "26px",
                                    paddingTop: "15.5px", paddingBottom: "15.5px"}}
                                onClick={() => alert("UNSUPPORTED YET")}/>
                            </div>
                    </div>

                </form>
            </div>
       )
    }
}

export default CompetitionParamsForm;