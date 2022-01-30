import React from "react";
import DefaultSubmitButton from "../../common/default-submit-button";
import {withRouter} from "../../../helpers/with-router";
import {useTranslation, withTranslation} from "react-i18next";
import './navbar-header.css';
import ApiHelper from "../../../helpers/api-helper";
import buttonUpImage from "../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../join-competition/join-competition-player-form/team-collection/buttonDown.png";
import profileImage from "./profile-image.png";
import exitImage from "./exit-image.png";
import {isTeacher} from "../../../helpers/role-helper";
import makeCancelable from "../../../helpers/cancellable-promise";
import {useNavigate} from "react-router";

const NavbarHeader = ({userHasTeacherRole = isTeacher, onNoNeedUpdateNavbar, needUpdate}) => {
    const { t, i18n } = useTranslation('translation');
    const navigate = useNavigate()

    let secondNavBarButton;
    if (userHasTeacherRole()) {
        secondNavBarButton = <DefaultSubmitButton
        text={t('navbar.header.create')}
        additionalClasses={['navbar-button']}
        onClick={ () => navigate('/competitions/create') }
        />
    } else {
        secondNavBarButton = <DefaultSubmitButton
        text={t('navbar.header.join')}
        additionalClasses={['navbar-button']}
        onClick={ () => navigate('/competitions/join') }
        />
    }

    return (
        <div className='navbar-header-fixed-top'>
            <div className='d-flex navbar-content-container'>
                <div>
                <DefaultSubmitButton
                    text={t('navbar.header.history')}
                    additionalClasses={['navbar-button']}
                    onClick={ () => navigate('/competitions/history') }
                />
                </div>
                <div>
                {secondNavBarButton}
                </div>

                <div className={'navbar-userinfo-container'}>
                    <UserInfo i18n={i18n} onRedirect={navigate} onNoNeedUpdateNavbar={onNoNeedUpdateNavbar} needUpdate={needUpdate}/>
                </div>
            </div>
        </div>
    )
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

    componentWillUnmount() {
        if (this.infoPromise) {
            this.infoPromise.cancel();
        }
    }

    showMenu = (event) => {
        event.preventDefault();

        this.setState(prevState => {
            return {isMenuOpened: !prevState.isMenuOpened};
            },
            () => {
                console.log(this.state);
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
            <div style={{"display": "flex"}}>
                <div style={{fontSize: "20px"}}>
                    {this.translateRole(this.state.userDesc)}
                </div>
                <div className={"d-flex"}>
                    <div className={"menu-button"} onClick={this.showMenu}>
                        <div className={"menu-button-content"}>
                            <button className={"profile-button"}><img src={image} alt={"unwind"} className={"profile-dropdown-img"}/></button>
                        </div>
                        <div style={{position: "relative", height: "0", width: "0"}}>
                            {res}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default withTranslation('translation')(withRouter(NavbarHeader));