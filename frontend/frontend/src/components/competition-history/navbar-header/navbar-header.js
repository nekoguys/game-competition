import React from "react";
import DefaultSubmitButton from "../../common/default-submit-button";
import {withRouter} from "react-router-dom";
import './navbar-header.css';
import isTeacher from "../../../helpers/role-helper";

class NavbarHeader extends React.Component {

    onCreateGameClick = () => {
        this.props.history.push('/competitions/create');
    };

    onGameHistoryClick = () => {
        this.props.history.push('/competitions/history');
    };

    onEnterGameClick = () => {
        this.props.history.push('/competitions/join')
    };

    render() {
        const buttonsStyle = {
            marginBottom: "0.7rem",
            height: "80%",
            borderRadius: "15px",
            flexGrow: "1",
            paddingLeft: "20px",
            paddingRight: "20px"
        };

        let navbarButton;
        if (isTeacher())
            navbarButton = <DefaultSubmitButton text="Создать игру" style={buttonsStyle} onClick={this.onCreateGameClick}/>;
        else
            navbarButton = <DefaultSubmitButton text="Войти в игру" style={buttonsStyle} onClick={this.onEnterGameClick}/>;

        return (
            <div className="navbar-header-fixed-top">
                <div className={"d-flex"} style={{marginTop: "20px", marginLeft: "40px"}}>
                    <DefaultSubmitButton text="История игр" style={{...buttonsStyle, marginRight: "50px"}} onClick={this.onGameHistoryClick}/>
                    {navbarButton}
                </div>
            </div>
        );
    }
}

export default withRouter(NavbarHeader);