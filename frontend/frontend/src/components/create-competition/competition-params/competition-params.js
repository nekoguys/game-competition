import React, {useEffect, useState} from "react";

import "./competition-params.css"
import {makeStartingCompetitionForm, toCompetitionFormJsonObject} from "../../../helpers/competition-params-helper";


const CompetitionParamsForm = ({onFormStateUpdated, initialState=makeStartingCompetitionForm()}) => {
    const [formState, setFormState] = useState(initialState);
    useEffect(() => {
        onFormStateUpdated(formState);
    }, [])

    useEffect(() => {
        onFormStateUpdated(formState);
        console.log(toCompetitionFormJsonObject(formState));
    }, [formState]);

    const updateFormStateField = (fieldName, value) => {
        setFormState({...formState, [fieldName]: value});
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
                    <input className={"full-width-row game-name-input default-form-row"} placeholder={"Название игры"}
                    onChange={(ev) => updateFormStateField("name", ev.target.value)}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle placeholder={"a;b;c -> ax^2 + bx + c"} title={"Формула затрат"}
                    onChangeCallback={makeChangeHandler("expensesFormula")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle placeholder={"a;b -> a - bP, P - цена"} title={"Формула спроса"}
                    onChangeCallback={makeChangeHandler("demandFormula")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Количество команд"} onChangeCallback={makeChangeHandler("maxTeamsAmount")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Максимальное количество игроков в команде"}
                                         onChangeCallback={makeChangeHandler("maxTeamSize")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Количество раундов"} onChangeCallback={makeChangeHandler("roundsCount")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Продолжительность раунда"} onChangeCallback={makeChangeHandler("roundLength")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Максимальный убыток"} onChangeCallback={makeChangeHandler("teamLossUpperbound")}/>
                </div>
                <div className={"game-form__field"}>
                    <InputFieldWithTitle title={"Инструкция для студентов"} onChangeCallback={makeChangeHandler("instruction")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField title={"Показывать студентам результаты предыдущих раундов"}
                    onChangeCallback={makeChangeHandler("shouldShowStudentPreviousRoundResults")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField title={"После окончания игры показывать студентам результаты всех команд"}
                    onChangeCallback={makeChangeHandler("shouldShowResultTableInEnd")}/>
                </div>
                <div className={"game-form__field"}>
                    <CheckboxField title={"Автоматическое проведение игры"}
                    onChangeCallback={makeChangeHandler("isAutoRoundEnding")}/>
                </div>
            </div>

        </form>
    )
}

const InputFieldWithTitle = ({title, placeholder="", onChangeCallback}) => {
    return (
        <div>
            <div>
                <span className={"game-form__input-field-title"}>{title}</span>
            </div>
            <div>
                <input placeholder={placeholder} className={"full-width-row default-form-row game-form__input"} onChange={(ev) => onChangeCallback(ev.target.value)}/>
            </div>
        </div>
    )
}

const CheckboxField = ({title, onChangeCallback}) => {
    return (
        <div>
            <label className={"checkbox-checkmark-container"}>{title}
                <input className={"checkbox-checkmark"} type={"checkbox"}
                       onChange={(event) => onChangeCallback(event.target.checked)}/>
                <span className={"checkmark"}/>
            </label>
        </div>
    )
}

export default CompetitionParamsForm;