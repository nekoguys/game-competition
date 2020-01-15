import React from "react";

import "./competition-params.css"
import DefaultTextInput from "../../common/default-text-input";
import DefaultCheckboxButton from "../../common/default-checkbox-button";

class CompetitionParamsForm extends React.Component {
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
            padding: "10px",
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
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label  text-right"}>Формула затрат</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Формула спроса</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Кол-во команд</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Игроков в команде</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Кол-во раундов</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Продолжительность рануда</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>
                    <div className={"form-group row"}>
                        <label className={"col-sm-3 col-form-label text-right"}>Инструкция для студентов</label>
                        <div className={"col-sm-9"}>
                            <DefaultTextInput style={defaultTextInputStyle}/>
                        </div>
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Показывать студентам результаты предыдущих раундов"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}/>
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"Не завершать раунд, пока все не отправили решение"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}/>
                    </div>

                    <div className={"form-group row"}>
                        <DefaultCheckboxButton notActiveColor={"#C4C4C4"} activeColor={"#3EE14E"}
                                               text={"После окончания показывать результаты всех команд"}
                                               style={checkboxButtonStyle} labelStyle={checkBoxButtonLabelStyle}/>
                    </div>

                </form>
            </div>
       )
    }
}

export default CompetitionParamsForm;