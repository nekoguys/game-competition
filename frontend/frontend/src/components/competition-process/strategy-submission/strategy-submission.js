import React, {useState} from "react";

import "./strategy-submission.css";
import {DefaultToggle} from "../competition-process-student/messages/messages";
import {useTranslation} from "react-i18next";

const StrategySubmissionComponent = ({
                                         strategyText,
                                         setStrategyText,
                                         onSubmit,
                                         didSubmitStrategy = false,//TODO
                                         isExpandedByDefault = false
                                     }) => {
    const [expanded, setExpanded] = useState(isExpandedByDefault);
    const {t} = useTranslation();
    let form, button;
    if (expanded) {
        form = (
            <div className={"form-group"}>
                <label htmlFor="strategy-text"
                       style={{paddingLeft: "3px", marginBottom: "3px", color: "rgba(0, 0, 0, 0.6)"}}>Введите
                    стратегию</label>
                <textarea className="form-control" id="strategy-text" rows="3" value={strategyText} onChange={el => {
                    setStrategyText(el.target.value);
                }}/>
            </div>
        );

        button = (
            <button
                className={"default-button strategy-submission-button"}
                onClick={() => onSubmit()}
            >
                {t("competition_process.student.send_strategy")}
            </button>
        )
    }

    return (
        <div>
            <DefaultToggle title={t("competition_process.student.strategy")} expanded={expanded}
                           setExpanded={setExpanded}/>
            <div className={"competition-process-student-toggle-content"}>
                {form}
                <div className={"strategy-submission-button-container"}>
                    {button}
                </div>
            </div>
        </div>
    )
}

export default StrategySubmissionComponent;
