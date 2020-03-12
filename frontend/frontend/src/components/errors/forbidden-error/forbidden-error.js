import React from "react";
import NavbarHeader from "../../competition-history/navbar-header";

class ForbiddenError extends React.Component {

    render() {
        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "320px", textAlign: "center", fontSize: "36px"}}>
                    <span>
                            Отказано в доступе
                        <br/>
                            403
                    </span>
                </div>
            </div>
        );
    }
}

export default ForbiddenError;