import React, {useRef} from "react";
import DefaultSubmitButton from "../../common/default-submit-button";
import {useTranslation} from "react-i18next";
import './navbar-header.css';
import {useNavigate} from "react-router";
import UserInfo from "./user-info";
import {LocalStorageWrapper} from "../../../helpers/storage-wrapper";

const NavbarHeader = ({needUpdateNavbarStorage, userInfoFetcher}) => {
    const { t } = useTranslation('translation');
    const navigate = useNavigate()

    const logoutAdditionalAction = () => {
        window.localStorage.removeItem("user_email");
        window.localStorage.removeItem("expirationTimestamp");
        window.localStorage.removeItem("roles");
        window.localStorage.removeItem("accessToken");
        window.localStorage.setItem("userDesc", "Guest");
    }

    const userDescLocalStorage = useRef(new LocalStorageWrapper("userDesc", ""));

    return (
        <div className='navbar-header-fixed-top'>
            <div className='navbar-content-container'>
                <div>
                <DefaultSubmitButton
                    text={t('navbar.header.back_to_main_page')}
                    additionalClasses={['navbar-button']}
                    onClick={ () => navigate('/competitions/history') }
                />
                </div>

                <div className={'navbar-userinfo-container'}>
                    <UserInfo
                        redirect={navigate}
                        logoutAdditionalAction={logoutAdditionalAction}
                        needUpdateNavbarStorage={needUpdateNavbarStorage}
                        userInfoFetcher={userInfoFetcher}
                        userDescStorage={userDescLocalStorage.current}
                    />
                </div>
            </div>
        </div>
    )
}

export default NavbarHeader;