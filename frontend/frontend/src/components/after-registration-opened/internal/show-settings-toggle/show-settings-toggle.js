import React from "react";

import "./show-settings-toggle.css";

const ShowSettingsToggle = ({t, isExpanded, setExpanded}) => {
    return (
        <div className={"show-settings-toggle-centered-inner-container"}>
            <div className={"show-settings-toggle-container"} onClick={() => setExpanded(!isExpanded)}>
                <div className={"show-settings-toggle-text"}>
                    {t('waiting_room.show_settings')}
                </div>
                <img className={"show-settings-toggle-unwind" + (isExpanded ? " show-settings-toggle-wind" : "")}
                    src={"data:image/svg+xml,%3Csvg width='17' height='13' viewBox='0 0 17 13' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 12L8.69231 2L16 12' stroke='black' stroke-opacity='0.4' stroke-width='1.4359'/%3E%3C/svg%3E%0A"}>
                </img>
            </div>
        </div>

    )
}

export default ShowSettingsToggle;
