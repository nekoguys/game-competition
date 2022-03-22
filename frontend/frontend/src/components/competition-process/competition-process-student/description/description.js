import React, {useState} from "react";
import {useTranslation} from "react-i18next";

import "./description.css";
import {DefaultToggle} from "../messages/messages";

const DescriptionHolder = ({instruction}) => {
    const [expanded, setExpanded] = useState(false)
    const {t} = useTranslation();

    let res;
    if (expanded) {
        res = (
            <div>
                <div className={"instruction-holder-top-spacer"}/>
                <div className={"instruction-holder"}>
                    {instruction}
                </div>
            </div>
        );
    }

    return (
        <div>
            <DefaultToggle title={t("competition_process.student.description.description")} expanded={expanded}
                           setExpanded={setExpanded}/>
            <div className={"competition-process-student-toggle-content"}>
                {res}
            </div>
        </div>
    )
}

export default DescriptionHolder;
