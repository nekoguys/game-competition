import React from "react";

import './header.css'

const Header = ({text, style, isSelected, onClick = () => {}}) => {
    const divClassName = "header" + (!isSelected ? " header_unselected" : " header_selected");
    return (
        <div className={divClassName} style={style} onClick={onClick}>
            <span className={"header-text"}> {text} </span>
        </div>
    )
}

export default Header;