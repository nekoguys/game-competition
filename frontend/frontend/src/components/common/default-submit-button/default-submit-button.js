import React from "react";

import "./default-submit-button.css"

const DefaultSubmitButton = ({text, style, onClick = () => {}}) => {
    return (
        <div className={"button-container"}>
            <button type={"button"} className={"btn btn-primary default-submit-button"}
                    style={style}
                    title={text}
                    onClick={onClick}>
                {text}
            </button>
        </div>
    )
};

export default DefaultSubmitButton;