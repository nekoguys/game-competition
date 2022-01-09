import React, {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {useNavigate, useParams} from "react-router";

const Verification = ({fetchers, showNotification}) => {
    const [isVerified, setVerified] = useState(false);
    const params = useParams();
    const navigate = useNavigate();
    const didStartVerification = useRef([]);
    const { t } = useTranslation("translation");

    useEffect(() => {
        console.log({token: params.token});
        verifyAccount(params.token);
    });

    const verifyAccount = (token) => {
        if (didStartVerification.current.includes(token)) {
            return;
        }
        didStartVerification.current.push(token);
        fetchers.verify(token)
            .then(resp => {
                setVerified(true);
                showNotification().success(resp.message, "Success", 2500);

                setTimeout(() => {
                    navigate("/auth/signin");
                }, 2500);
            })
            .catch(err => {
                showNotification().error(err.message || "Error Occurred", "Error", 2500);
            });
    }

    const res = isVerified ? t('auth.verification.done') : t('auth.verification.process');

    return (
        <div>
            <div style={{fontSize: "30px"}}>
                {res}
            </div>
        </div>
    )
}

export default Verification;
