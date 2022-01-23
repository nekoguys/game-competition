import React from "react";
import DefaultSubmitButton from "../../common/default-submit-button";
import {withRouter} from "../../../helpers/with-router";
import {withTranslation} from "react-i18next";
import './navbar-header.css';
import ApiHelper from "../../../helpers/api-helper";
import buttonUpImage from "../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../join-competition/join-competition-player-form/team-collection/buttonDown.png";
import profileImage from "./profile-image.png";
import exitImage from "./exit-image.png";
import {isTeacher} from "../../../helpers/role-helper";
import makeCancelable from "../../../helpers/cancellable-promise";

class NavbarHeader extends React.Component {

    onCreateGameClick = () => {
        this.props.history('/competitions/create');
    };

    onGameHistoryClick = () => {
        this.props.history('/competitions/history');
    };

    onRedirect = (path) => {
        this.props.history(path);
    };
    onEnterGameClick = () => {
        this.props.history('/competitions/join')
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
        const { i18n } = this.props;
        let navbarButton;
        if (isTeacher())
            navbarButton = <DefaultSubmitButton text={i18n.t('navbar.header.create')} style={buttonsStyle} onClick={this.onCreateGameClick}/>;
        else
            navbarButton = <DefaultSubmitButton text={i18n.t('navbar.header.join')} style={buttonsStyle} onClick={this.onEnterGameClick}/>;

        return (
            <div className="navbar-header-fixed-top">
                <div className={"d-flex"} style={{marginTop: "20px", marginLeft: "40px"}}>
                    <DefaultSubmitButton text={i18n.t('navbar.header.history')} style={{...buttonsStyle, marginRight: "50px"}} onClick={this.onGameHistoryClick}/>
                    {navbarButton}


                    <div style={{marginLeft: "auto"}}>
                        <div style={{paddingRight: "50px"}}>
                        <UserInfo i18n={i18n} onRedirect={this.onRedirect} onNoNeedUpdateNavbar={this.props.onNoNeedUpdateNavbar} needUpdate={this.props.needUpdate}/>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

class UserInfo extends React.Component {

    translateRole = (userDesc) => {
        if (window.localStorage.getItem("language") === "en") {
            const split = userDesc.split('-');
            const role = split[split.length - 1];
            const prefix = userDesc.substring(0, userDesc.length - role.length);
            console.log({role});
            if (role.toLowerCase().endsWith("админ")) {
                return prefix + " Admin";
            } else if (role.toLowerCase().endsWith("учитель")) {
                return prefix + " Teacher"
            } else {
                return prefix + " Student"
            }
        }
        return userDesc;
    };

    constructor(props) {
        super(props);

        let userDesc = window.localStorage.getItem("userDesc");

        if (!userDesc) {
            userDesc = "";
        }

        this.state = {
            userDesc: userDesc,
            isMenuOpened: false
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.needUpdate) {
            this.props.onNoNeedUpdateNavbar();
            this.setupNavBarData();
        }
    }

    componentDidMount() {
        this.setupNavBarData();
    }

    setupNavBarData() {
        console.log("update navbar");

        this.infoPromise = makeCancelable(ApiHelper.getNavBarInfo());
        this.infoPromise.promise.then((resp) => {
            if (resp.status < 300) {
                resp.json().then(jsonBody => {
                    window.localStorage.setItem("userDesc", jsonBody.userDescription + " - " + jsonBody.role);
                    this.setState({userDesc: jsonBody.userDescription + " - " + jsonBody.role});
                })
            } else {
                resp.text().then(txt => {
                    console.log(txt)
                });
            }
        }).catch(err => {
            console.log(err);
        })
    }
    closeMenu = (event) => {
        console.log({closeMenuState: this.state});
        if (this.state.isMenuOpened && !this.dropdownMenu.contains(event.target)) {

            this.setState({isMenuOpened: false}, () => {
                document.removeEventListener('click', this.closeMenu);
            });
        }
    };

    componentWillUnmount() {
        document.removeEventListener('click', this.closeMenu);
        if (this.infoPromise) {
            this.infoPromise.cancel();
        }
    }

    showMenu = (event) => {
        event.preventDefault();

        this.setState(prevState =>{ return {isMenuOpened: !prevState.isMenuOpened};}, () => {
            console.log(this.state);
            if (this.state.isMenuOpened) {
                document.addEventListener('click', this.closeMenu);
            } else {
                document.removeEventListener('click', this.closeMenu);
            }
        });
    };

    render() {
        let image;
        let res;
        const { i18n } = this.props;

        if (this.state.isMenuOpened) {
            image = buttonDownImage;
            res = (
                <div className={"dropdown-content"} ref={(element) => {
                    this.dropdownMenu = element;
                }}>
                    <div className={"dropdown-element"} onClick={() => {
                        this.props.onRedirect("/profile")
                    }}>
                        <div className={"d-flex"}>
                            <div style={{paddingRight: "7px"}}>
                                <img src={profileImage} width={"24px"} height={"24px"}/>
                            </div>
                            <div style={{textAlign: "center"}}>
                            {i18n.t('navbar.header.profile')}
                            </div>
                        </div>
                    </div>
                    <div className={"dropdown-element"} onClick={() => {
                        window.localStorage.removeItem("user_email");
                        window.localStorage.removeItem("expirationTimestamp");
                        window.localStorage.removeItem("roles");
                        window.localStorage.removeItem("accessToken");
                        window.localStorage.setItem("userDesc", "Guest");
                        this.props.onRedirect("/");
                    }}>
                        <div className={"d-flex"}>
                            <div style={{paddingRight: "7px"}}>
                                <img src={exitImage} width={"24px"} height={"24px"}/>
                            </div>
                            <div style={{margin: "0 auto"}}>
                                {i18n.t('navbar.header.exit')}
                            </div>
                        </div>
                    </div>
                </div>
            )
        } else {
            image = buttonUpImage;
        }

        return (
            <div className={"row"}>
                <div style={{fontSize: "20px"}}>
                    {this.translateRole(this.state.userDesc)}
                </div>
                <div>
                    <div className={"menu-button"} onClick={this.showMenu}>
                        <div className={"menu-button-content"}>
                            <button style={{
                                border: "none",
                                backgroundColor: "Transparent",
                                transform: "scale(0.35) translate(-5px, 0)",
                                width: "50px",
                                height: "50px"
                            }}><img src={image} alt={"unwind"}/></button>
                        </div>
                    </div>
                    <div style={{position: "relative", display: "inline-block"}}>
                    {res}
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation('translation')(withRouter(NavbarHeader));