import React, {useEffect} from "react";

import "./competition-params.css"
import {makeStartingCompetitionForm} from "../../../helpers/competition-params-helper";


const CompetitionParamsForm = ({onFormStateUpdated, state = makeStartingCompetitionForm()}) => {
    const formState = state;
    useEffect(() => {
        onFormStateUpdated(formState);
    }, [])

    const updateFormStateField = (fieldName, value) => {
        onFormStateUpdated({...formState, [fieldName]: value})
    }

    const makeChangeHandler = (name) => {
        return (value) => {
            updateFormStateField(name, value)
        }
    }

    return (
        <form>
            <div className={"game-form__fields-container"}>
                <div className={"game-form__field"}>
                    <input value={formState.name} className={"full-width-row game-name-input default-form-row"} placeholder={"Название игры"}
                    onChange={(ev) => updateFormStateField("name", ev.target.value)}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.expensesFormula} placeholder={"a;b;c -> ax^2 + bx + c"} title={"Формула затрат"}
                    onChangeCallback={makeChangeHandler("expensesFormula")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.demandFormula} placeholder={"a;b -> a - bP, P - цена"} title={"Формула спроса"}
                    onChangeCallback={makeChangeHandler("demandFormula")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.maxTeamsAmount} title={"Количество команд"} onChangeCallback={makeChangeHandler("maxTeamsAmount")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.maxTeamSize} title={"Максимальное количество игроков в команде"}
                                         onChangeCallback={makeChangeHandler("maxTeamSize")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.roundsCount} title={"Количество раундов"} onChangeCallback={makeChangeHandler("roundsCount")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.roundLength} title={"Продолжительность раунда"} onChangeCallback={makeChangeHandler("roundLength")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.teamLossUpperbound} title={"Максимальный убыток"} onChangeCallback={makeChangeHandler("teamLossUpperbound")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle value={formState.instruction} title={"Инструкция для студентов"} onChangeCallback={makeChangeHandler("instruction")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField checked={formState.shouldShowStudentPreviousRoundResults} title={"Показывать студентам результаты предыдущих раундов"}
                    onChangeCallback={makeChangeHandler("shouldShowStudentPreviousRoundResults")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField checked={formState.shouldShowResultTableInEnd} title={"После окончания игры показывать студентам результаты всех команд"}
                    onChangeCallback={makeChangeHandler("shouldShowResultTableInEnd")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField checked={formState.isAutoRoundEnding} title={"Автоматическое проведение игры"}
                    onChangeCallback={makeChangeHandler("isAutoRoundEnding")}/>
                </div>
            </div>

        </form>
    )
}

const InputFieldWithTitle = ({title, value, placeholder="", onChangeCallback}) => {
    return (
        <div>
            <div>
                <span className={"game-form__input-field-title"}>{title}</span>
            </div>
            <div>
                <input value={value} placeholder={placeholder} className={"full-width-row default-form-row game-form__input"} onChange={(ev) => onChangeCallback(ev.target.value)}/>
            </div>
        </div>
    )
}

const CheckboxField = ({title, checked, onChangeCallback}) => {
    return (
        <div>
            <label className={"checkbox-checkmark-container"}>{title}
                <input checked={checked} className={"checkbox-checkmark"} type={"checkbox"}
                       onChange={(event) => onChangeCallback(event.target.checked)}/>
                <span className={"checkmark"}/>
            </label>
        </div>
    )
}

export default CompetitionParamsForm;