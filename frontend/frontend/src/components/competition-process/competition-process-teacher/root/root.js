import React, {useEffect, useState} from "react";

import "./root.css";
import {CompetitionProcessTeacherBodyNew, NavbarHeaderWithFetcher} from "../../../app/app";
import withRedirect from "../../../../helpers/redirect-helper";
import {useNavigate, useParams} from "react-router";
import withAuthenticated from "../../../../helpers/with-authenticated";

const CompetitionProcessTeacherRootNew = ({showNotification}) => {
    const [didEnd, setDidEnd] = useState(false);
    const [competitionName, setCompetitionName] = useState("Конкуренция на рынке пшеницы");
    const navigate = useNavigate();
    const {pin} = useParams();

    useEffect((prevDidEnd) => {
        if (didEnd && !prevDidEnd) {
            showNotification().success("Attention", "Game has ended", 2500);
            setTimeout(() => {
                navigate("/competitions/results/" + pin);
            }, 2500);
        }
    }, [didEnd])

    return (
        <div>
            <div>
                <NavbarHeaderWithFetcher/>
            </div>
            <div className={"below-navbar"}>
                <div className={"page-title competition-process-student-page-title"}>
                    {competitionName + " №" + pin}
                </div>

                <div>
                    <CompetitionProcessTeacherBodyNew showNotification={showNotification}
                                                      onEndCallback={() => setDidEnd(true)} pin={pin}
                                                      updateCompetitionNameCallback={(name) => setCompetitionName(name)}/>
                </div>
            </div>
        </div>
    )
}

export default withAuthenticated(withRedirect(CompetitionProcessTeacherRootNew));
