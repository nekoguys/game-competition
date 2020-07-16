import React from "react";
import buttonUpImage from "../../join-competition/join-competition-player-form/team-collection/buttonUp.png";
import buttonDownImage from "../../join-competition/join-competition-player-form/team-collection/buttonDown.png";

import "./strategy-submission.css";
import DefaultSubmitButton from "../../common/default-submit-button";

class StrategySubmissionComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isExpanded: props.isExpanded ?? false,
        };

        this.strategyText = "";
    }

    onSubmitButtonClicked = () => {
        this.props.onSubmit(this.strategyText);
    }

    onTextChanges = () => {
        const {onChange=() => {}} = this.props;

        onChange(this.strategyText);
    }

    render() {
        const image = this.state.isExpanded ? buttonDownImage : buttonUpImage;

        let form;
        let button;
        if (this.state.isExpanded) {
            form = (
                <div className={"form-group"}>
                    <label htmlFor="strategy-text" style={{paddingLeft: "3px", marginBottom: "3px"}}>Введите стратегию</label>
                    <textarea className="form-control" id="strategy-text" rows="3" defaultValue={this.props.defaultText} onChange={el => {
                        this.strategyText = el.target.value.toString();
                        this.onTextChanges();
                    }}/>
                </div>
            );

            button = (
                <div style={{paddingTop: "10px"}} className={"row justify-content-center"}>
                    <div style={{width: "20%", minWidth: "15em"}}>
                    <DefaultSubmitButton text={"Отправить стратегию"} onClick={this.onSubmitButtonClicked}/>
                    </div>
                </div>
            )
        }

        return (
            <div>
                <div style={{width: "100%", paddingLeft: "30px", paddingRight: "30px"}}>
                    <div className={"show-messages row justify-content-center"} style={{width: "20%", margin: "0 auto", position: "relative"}}
                             onClick={() => this.setState(prevState => {
                         return {isExpanded: !prevState.isExpanded};
                     })}><div className={"col-7"} style={{padding: 0, margin: "5px 0"}}>
                            Стратегия
                        </div>
                            <div className={"col-2"} style={{padding: 0, height: 0}}>
                            <button style={{
                                border: "none",
                                backgroundColor: "Transparent",
                                transform: "scale(0.35) translate(-20px, -30%)"
                            }}><img src={image} alt={"unwind"}/></button>
                            </div>
                    </div>
                    <div style={{paddingTop: "10px"}}>
                        <div className={"form-holder"}>
                            {form}
                        </div>
                    </div>
                    {button}
                </div>
            </div>
        )
    }
}

export default StrategySubmissionComponent;
