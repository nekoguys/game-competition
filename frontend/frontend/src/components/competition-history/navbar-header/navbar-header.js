import React from "react";
import DefaultSubmitButton from "../../common/default-submit-button";
import './navbar-header.css';

class NavbarHeader extends React.Component {
    render() {
        const buttonsStyle = {
            marginBottom: "0.7rem",
            height: "80%",
            borderRadius: "15px",
            flexGrow: "1",
            paddingLeft: "20px",
            paddingRight: "20px"
        };
        return (
            <div className="navbar-header-fixed-top">
                <div className={"d-flex"} style={{marginTop: "20px", marginLeft: "40px"}}>
                    <DefaultSubmitButton text="История игр" style={{...buttonsStyle, marginRight: "50px"}}/>
                    <DefaultSubmitButton text="Создать игру" style={buttonsStyle}/>
                </div>
            </div>
        )
    }
}

export default NavbarHeader;