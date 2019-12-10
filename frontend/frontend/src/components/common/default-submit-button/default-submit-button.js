import React from "react";

import "./default-submit-button.css"

const DefaultSubmitButton = ({text, style, isDisabled = false, onClick = () => {}}) => {
    return (
        <div className={"button-container"}>
            <button type={"button"} className={"btn btn-primary default-submit-button"}
                    style={style}
                    title={text}
                    onClick={onClick}
                    disabled={isDisabled}>
                {text}
            </button>
        </div>
    )
};

export default DefaultSubmitButton;