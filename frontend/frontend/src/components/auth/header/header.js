import React from "react";

import './header.css'
import * as PropTypes from "prop-types";

class Header extends React.Component {

    render() {
        let {text, style, isSelected, onClick = () => {}} = this.props;
        let divClassName = "header";
        if (!isSelected) {
            divClassName += " unselected";
        }
        return <div className={divClassName} style={style} onClick={onClick}><span className={"header-text"}> {text} </span></div>
    }
}

Header.propTypes = {
    text: PropTypes.string,
    style: PropTypes.any,
    isSelected: PropTypes.bool,
    onClick: PropTypes.func
};

export default Header;