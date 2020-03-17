import React from "react";
import {withRouter} from "react-router-dom";
import ApiHelper from "../../../helpers/api-helper";
import showNotification from "../../../helpers/notification-helper";

class Verification extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isVerified: false
        }
    }

    componentDidMount() {
        this.verifyAccount();
    }

    verifyAccount() {
        const {token} = this.props.match.params;
        ApiHelper.accountVerification(token).then(resp => {
            if (resp.status < 300) {
                resp.json().then(jsonBody => {
                    this.setState({isVerified: true});
                    showNotification(this).success(jsonBody.message, "Success", 2500);

                    setTimeout(() => {
                        this.props.history.push("/auth/signin");
                    }, 2500);
                })
            } else {
                resp.text().then(text => {
                    showNotification(this).error(text, "Error", 2500);
                })
            }
        })
    }

    render() {

        let res = "Идет подтверждение аккаунта";
        if (this.state.isVerified) {
            res = "Аккаунт успешно подтвержден!";
        }

        return (
            <div>
                <div style={{fontSize: "30px"}}>
                    {res}
                </div>
            </div>
        )
    }
}

export default withRouter(Verification);
