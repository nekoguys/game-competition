import React from "react";
import {useTranslation} from "react-i18next";

import "./teacher-create-competition-header.css";

const TeacherCreateCompetitionHeader = ({onClickAction}) => {
    const {t} = useTranslation();
    return (
        <div className={"create-competition-button-container"}>
            <button className={"create-competition-button page-title"} onClick={onClickAction}>
                {t("navbar.header.create")}
            </button>
        </div>
    )
};

export default TeacherCreateCompetitionHeader;
