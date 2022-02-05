import React, {useEffect, useState} from "react";

import "./user-info.css";
import {useTranslation} from "react-i18next";
import profileImage from "../profile-image.png";
import exitImage from "../exit-image.png";
import buttonUpImage from "../../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../../join-competition/join-competition-player-form/team-collection/buttonDown.png";
import {FieldStorageWrapper} from "../../../../helpers/storage-wrapper";
import useDidMountHook from "../../../../helpers/use-did-mount-hook";

const UserInfo = ({redirect, logoutAdditionalAction, userInfoFetcher, needUpdateStorage = new FieldStorageWrapper(false), userDescStorage}) => {
    const [userDesc, setUserDesc] = useState(userDescStorage.get());
    const [isMenuOpened, setIsMenuOpened] = useState(false);

    const fetchUserInfo = () => {
        userInfoFetcher().then(resp => {
            setUserDesc(resp.userDescription);
        }).catch(err => {
            console.log(err);
        })
    }

    const showMenu = (event) => {
        event.preventDefault();

        setIsMenuOpened(!isMenuOpened);
    };

    useEffect(() => {
        if (userDesc) {
            userDescStorage.set(userDesc);
        }
    }, [userDesc]);

    useEffect(() => {
       if (needUpdateStorage.get())  {
            needUpdateStorage.set(false);
            fetchUserInfo();
       }
    });

    useDidMountHook(() => {
        if (!userDescStorage.get() && !needUpdateStorage.get()) {
            fetchUserInfo();
        }
    })

    const image = isMenuOpened ? buttonDownImage : buttonUpImage;
    const menu = isMenuOpened ? <Menu logoutAdditionalAction={logoutAdditionalAction} redirect={redirect}/> : null;
    return (
        <div style={{"display": "flex"}}>
            <div className={"user-info"}>
                {userDesc}
            </div>
            <div className={"d-flex"}>
                <div className={"menu-button"} onClick={showMenu}>
                    <div className={"menu-button-content"}>
                        <button className={"profile-button"}><img src={image} alt={"unwind"} className={"profile-dropdown-img"}/></button>
                    </div>
                    <div style={{position: "relative", height: "0", width: "0"}}>
                        {menu}
                    </div>
                </div>
            </div>
        </div>
    )
}

const Menu = ({logoutAdditionalAction, redirect}) => {
    const {t} = useTranslation();

    return (
        <div className={"dropdown-content"}>
            <div className={"dropdown-element"} onClick={() => {
                redirect("/profile")
            }}>
                <div className={"d-flex"}>
                    <div style={{paddingRight: "7px"}}>
                        <img src={profileImage} width={"24px"} height={"24px"}/>
                    </div>
                    <div style={{textAlign: "center"}}>
                        {t('navbar.header.profile')}
                    </div>
                </div>
            </div>
            <div className={"dropdown-element"} onClick={() => {
                logoutAdditionalAction();
                redirect("/");
            }}>
                <div className={"d-flex"}>
                    <div style={{paddingRight: "7px"}}>
                        <img src={exitImage} width={"24px"} height={"24px"}/>
                    </div>
                    <div style={{margin: "0 auto"}}>
                        {t('navbar.header.exit')}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default UserInfo;
