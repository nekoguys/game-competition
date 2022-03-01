import React from "react";

import "./default-submit-button.css"

const DefaultSubmitButton = ({text, style, additionalClasses = [], isDisabled = false, onClick = () => {}}) => {
    return (
            <button type={"button"} className={"btn default-submit-button " + additionalClasses.join(' ')}
                    style={style}
                    title={text}
                    onClick={onClick}
                    disabled={isDisabled}>
                {text}
            </button>
    )
};

export default DefaultSubmitButton;